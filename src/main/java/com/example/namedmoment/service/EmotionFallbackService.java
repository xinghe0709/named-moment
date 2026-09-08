package com.example.namedmoment.service;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.ConceptRanking;
import com.example.namedmoment.dto.EmotionConceptToolItem;
import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.tool.EmotionConceptTools;
import com.example.namedmoment.utils.MatchResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class EmotionFallbackService {

    @Resource(name = "fallbackChatClient")
    private ChatClient fallbackChatClient;

    @Resource
    private ObjectProvider<EmotionConceptTools> emotionConceptToolsProvider;

    @Resource
    private ObjectMapper objectMapper;

    @Value("classpath:prompts/emotion-fallback.st")
    private org.springframework.core.io.Resource fallbackPrompt;

    @Value("classpath:prompts/emotion-tool-rerank.st")
    private org.springframework.core.io.Resource toolRerankPrompt;

    public List<ConceptMatch> match(EmotionFingerprint fingerprint) {
        EmotionConceptTools tool = emotionConceptToolsProvider.getObject();
        try {
            String fingerprintPayload = objectMapper.writeValueAsString(fingerprint);
            requestToolSearch(fingerprintPayload, tool);
            List<EmotionConceptToolItem> candidates = tool.getReturnedConcepts();
            if (candidates.size() < AppConstants.MATCH_RESULT_LIMIT) {
                log.warn("stage=tool-search status=failed returnedConceptCount={}",
                        candidates.size());
                throw new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);
            }

            String rankingPayload = buildRankingPayload(fingerprint, candidates);
            Set<Long> returnedConceptIds = tool.getReturnedConceptIds();
            Map<Long, String> candidateNames = new LinkedHashMap<Long, String>();
            for (EmotionConceptToolItem candidate : candidates) {
                candidateNames.put(candidate.getConceptId(), candidate.getName());
            }
            BusinessException validationException = null;
            for (int attempt = 1;
                 attempt <= AppConstants.AI_SEMANTIC_MAX_ATTEMPTS; attempt++) {
                ConceptRanking ranking = requestRanking(rankingPayload);
                List<ConceptMatch> matches = ranking == null ? null : ranking.getMatches();
                try {
                    return MatchResultUtils.validateAndSort(
                            matches, returnedConceptIds, candidateNames);
                } catch (BusinessException exception) {
                    validationException = exception;
                    log.warn("stage=tool-validation status=retry attempt={} "
                                    + "matchCount={} returnedConceptCount={}",
                            attempt, matches == null ? 0 : matches.size(),
                            returnedConceptIds.size());
                }
            }
            throw validationException == null
                    ? new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED)
                    : validationException;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("stage=tool-fallback status=failed type={}",
                    exception.getClass().getSimpleName());
            if (containsDatabaseFailure(exception)) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR);
            }
            throw new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);
        }
    }

    void requestToolSearch(String payload, EmotionConceptTools tool) {
        fallbackChatClient.prompt()
                .system(fallbackPrompt)
                .user("待匹配的情感指纹：\n" + payload)
                .tools(tool)
                .call()
                .content();
    }

    ConceptRanking requestRanking(String payload) {
        return fallbackChatClient.prompt()
                .system(toolRerankPrompt)
                .user("情感指纹与工具候选：\n" + payload)
                .call()
                .entity(ConceptRanking.class,
                        spec -> spec.useProviderStructuredOutput().validateSchema());
    }

    private String buildRankingPayload(EmotionFingerprint fingerprint,
                                       List<EmotionConceptToolItem> candidates) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("fingerprint", fingerprint);
        payload.put("candidates", candidates);
        return objectMapper.writeValueAsString(payload);
    }

    private boolean containsDatabaseFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DataAccessException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
