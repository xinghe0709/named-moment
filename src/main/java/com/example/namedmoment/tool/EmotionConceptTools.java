package com.example.namedmoment.tool;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.dto.EmotionConceptToolItem;
import com.example.namedmoment.dto.EmotionConceptToolRequest;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EmotionConceptTools {

    @Resource
    private EmotionConceptMapper emotionConceptMapper;

    private final Set<Long> returnedConceptIds = new HashSet<Long>();

    @Tool(description = "按情绪、场景和心理体验关键词查询数据库中真实存在的情感概念")
    public List<EmotionConceptToolItem> searchEmotionConcepts(
            EmotionConceptToolRequest request) {
        List<String> keywords = normalizeAndValidate(request);
        List<EmotionConcept> concepts = emotionConceptMapper.searchByKeywords(
                keywords, AppConstants.TOOL_QUERY_LIMIT);

        List<EmotionConceptToolItem> items = new ArrayList<EmotionConceptToolItem>();
        for (EmotionConcept concept : concepts) {
            returnedConceptIds.add(concept.getId());
            items.add(EmotionConceptToolItem.builder()
                    .conceptId(concept.getId())
                    .name(concept.getName())
                    .language(concept.getLanguage())
                    .meaning(concept.getMeaning())
                    .description(concept.getDescription())
                    .build());
        }
        return items;
    }

    private List<String> normalizeAndValidate(EmotionConceptToolRequest request) {
        if (request == null || request.getKeywords() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Set<String> uniqueKeywords = new LinkedHashSet<String>();
        for (String keyword : request.getKeywords()) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                uniqueKeywords.add(keyword.trim());
            }
        }

        if (uniqueKeywords.isEmpty()
                || uniqueKeywords.size() > AppConstants.TOOL_KEYWORD_LIMIT) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        for (String keyword : uniqueKeywords) {
            if (keyword.length() < AppConstants.TOOL_KEYWORD_MIN_LENGTH
                    || keyword.length() > AppConstants.TOOL_KEYWORD_MAX_LENGTH) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
        }
        return new ArrayList<String>(uniqueKeywords);
    }
}
