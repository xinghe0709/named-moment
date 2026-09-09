package com.example.namedmoment.service;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.dto.ConceptCandidate;
import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.dto.EmotionMatchCandidateResponse;
import com.example.namedmoment.dto.EmotionMatchResponse;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.enums.MatchMode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import com.example.namedmoment.utils.EmbeddingTextUtils;
import com.example.namedmoment.utils.ExceptionLogUtils;
import com.example.namedmoment.utils.ExplanationTextUtils;
import com.example.namedmoment.utils.VectorUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmotionMatchService {

    @Resource
    private EmotionFingerprintService emotionFingerprintService;

    @Resource
    private EmotionRankingService emotionRankingService;

    @Resource
    private EmotionFallbackService emotionFallbackService;

    @Resource
    private EmotionConceptMapper emotionConceptMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    public EmotionMatchResponse match(String inputText) {
        long startedAt = System.nanoTime();
        log.info("stage=match status=started inputLength={}", inputText.length());
        EmotionFingerprint fingerprint = emotionFingerprintService.analyze(inputText);
        try {
            EmotionMatchResponse response = matchByRag(fingerprint);
            logMatchSuccess(response, startedAt);
            return response;
        } catch (Exception ragException) {
            log.warn("stage=rag status=fallback type={} rootType={} rootMessage={}",
                    ragException.getClass().getSimpleName(),
                    ExceptionLogUtils.rootType(ragException),
                    ExceptionLogUtils.rootMessage(ragException));
            try {
                EmotionMatchResponse response = matchByTool(fingerprint);
                logMatchSuccess(response, startedAt);
                return response;
            } catch (Exception fallbackException) {
                log.warn("stage=tool-fallback status=failed type={} rootType={} "
                                + "rootMessage={} durationMs={}",
                        fallbackException.getClass().getSimpleName(),
                        ExceptionLogUtils.rootType(fallbackException),
                        ExceptionLogUtils.rootMessage(fallbackException),
                        elapsedMs(startedAt));
                if (containsDatabaseFailure(fallbackException)) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, fallbackException);
                }
                throw new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED, fallbackException);
            }
        }
    }

    private EmotionMatchResponse matchByRag(EmotionFingerprint fingerprint) {
        String embeddingText = EmbeddingTextUtils.build(
                fingerprint.getMeaning(), fingerprint.getDescription());
        String vector = VectorUtils.toPgVector(embeddingModel.embed(embeddingText));
        List<ConceptCandidate> recalled = emotionConceptMapper.searchSimilar(
                vector, AppConstants.VECTOR_RECALL_LIMIT);

        List<ConceptCandidate> validCandidates = new ArrayList<ConceptCandidate>();
        if (recalled != null) {
            for (ConceptCandidate candidate : recalled) {
                if (candidate.getVectorScore() != null
                        && candidate.getVectorScore() >= AppConstants.MIN_VECTOR_SCORE) {
                    validCandidates.add(candidate);
                }
            }
        }
        if (validCandidates.size() < AppConstants.MATCH_RESULT_LIMIT) {
            log.warn("stage=vector-recall status=failed recalledCount={} validCount={}",
                    recalled == null ? 0 : recalled.size(), validCandidates.size());
            throw new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);
        }

        log.info("stage=vector-recall status=success recalledCount={} validCount={}",
                recalled.size(), validCandidates.size());

        List<ConceptMatch> matches = emotionRankingService.rank(fingerprint, validCandidates);
        return buildResponse(fingerprint, MatchMode.RAG, matches);
    }

    private EmotionMatchResponse matchByTool(EmotionFingerprint fingerprint) {
        List<ConceptMatch> matches = emotionFallbackService.match(fingerprint);
        return buildResponse(fingerprint, MatchMode.TOOL_FALLBACK, matches);
    }

    private EmotionMatchResponse buildResponse(EmotionFingerprint fingerprint,
                                               MatchMode matchMode,
                                               List<ConceptMatch> matches) {
        List<Long> conceptIds = new ArrayList<Long>();
        for (ConceptMatch match : matches) {
            conceptIds.add(match.getConceptId());
        }

        List<EmotionConcept> concepts = emotionConceptMapper.selectByIds(conceptIds);
        Map<Long, EmotionConcept> conceptMap = new HashMap<Long, EmotionConcept>();
        if (concepts != null) {
            for (EmotionConcept concept : concepts) {
                conceptMap.put(concept.getId(), concept);
            }
        }

        List<EmotionMatchCandidateResponse> responses = new ArrayList<EmotionMatchCandidateResponse>();
        for (ConceptMatch match : matches) {
            EmotionConcept concept = conceptMap.get(match.getConceptId());
            if (concept == null) {
                throw new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);
            }
            responses.add(EmotionMatchCandidateResponse.builder()
                    .conceptId(concept.getId())
                    .name(concept.getName())
                    .language(concept.getLanguage())
                    .meaning(concept.getMeaning())
                    .description(concept.getDescription())
                    .sourceUrl(concept.getSourceUrl())
                    .matchScore(match.getMatchScore())
                    .explanation(ExplanationTextUtils.normalize(
                            match.getExplanation()))
                    .build());
        }

        return EmotionMatchResponse.builder()
                .fingerprint(fingerprint)
                .matchMode(matchMode)
                .candidates(responses)
                .build();
    }

    private boolean containsDatabaseFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DataAccessException) {
                return true;
            }
            if (current instanceof BusinessException
                    && ErrorCode.DATABASE_ERROR == ((BusinessException) current).getErrorCode()) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void logMatchSuccess(EmotionMatchResponse response, long startedAt) {
        int candidateCount = response.getCandidates() == null
                ? 0 : response.getCandidates().size();
        log.info("stage=match status=success mode={} candidateCount={} durationMs={}",
                response.getMatchMode(), candidateCount, elapsedMs(startedAt));
    }

    private long elapsedMs(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
