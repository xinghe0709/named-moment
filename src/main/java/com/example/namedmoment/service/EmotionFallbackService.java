package com.example.namedmoment.service;

import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.ConceptRanking;
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

import java.util.List;

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

    public List<ConceptMatch> match(EmotionFingerprint fingerprint) {
        EmotionConceptTools tool = emotionConceptToolsProvider.getObject();
        try {
            String payload = objectMapper.writeValueAsString(fingerprint);
            ConceptRanking ranking = requestRanking(payload, tool);
            return MatchResultUtils.validateAndSort(
                    ranking.getMatches(), tool.getReturnedConceptIds());
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

    ConceptRanking requestRanking(String payload, EmotionConceptTools tool) {
        return fallbackChatClient.prompt()
                .system(fallbackPrompt)
                .user("待匹配的情感指纹：\n" + payload)
                .tools(tool)
                .call()
                .entity(ConceptRanking.class, spec -> spec.validateSchema());
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
