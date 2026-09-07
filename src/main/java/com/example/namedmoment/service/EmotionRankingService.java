package com.example.namedmoment.service;

import com.example.namedmoment.dto.ConceptCandidate;
import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.ConceptRanking;
import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.utils.MatchResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class EmotionRankingService {

    @Resource(name = "analysisChatClient")
    private ChatClient analysisChatClient;

    @Resource
    private ObjectMapper objectMapper;

    @Value("classpath:prompts/emotion-rerank.st")
    private org.springframework.core.io.Resource rerankPrompt;

    public List<ConceptMatch> rank(EmotionFingerprint fingerprint,
                                   List<ConceptCandidate> candidates) {
        try {
            String payload = buildPayload(fingerprint, candidates);
            ConceptRanking ranking = requestRanking(payload);
            Set<Long> allowedIds = new HashSet<Long>();
            for (ConceptCandidate candidate : candidates) {
                allowedIds.add(candidate.getId());
            }
            return MatchResultUtils.validateAndSort(ranking.getMatches(), allowedIds);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("stage=rerank status=failed type={}",
                    exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);
        }
    }

    ConceptRanking requestRanking(String payload) {
        return analysisChatClient.prompt()
                .system(rerankPrompt)
                .user("情感指纹与候选概念：\n" + payload)
                .call()
                .entity(ConceptRanking.class, spec -> spec.validateSchema());
    }

    private String buildPayload(EmotionFingerprint fingerprint,
                                List<ConceptCandidate> candidates) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("fingerprint", fingerprint);
        payload.put("candidates", candidates);
        return objectMapper.writeValueAsString(payload);
    }
}
