package com.example.namedmoment.tool;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.dto.EmotionConceptToolItem;
import com.example.namedmoment.dto.EmotionConceptToolRequest;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmotionConceptToolsTest {

    @Mock
    private EmotionConceptMapper emotionConceptMapper;

    private EmotionConceptTools tools;

    @BeforeEach
    void setUp() {
        tools = new EmotionConceptTools();
        ReflectionTestUtils.setField(tools, "emotionConceptMapper", emotionConceptMapper);
    }

    @Test
    void shouldRejectMoreThanFiveKeywords() {
        EmotionConceptToolRequest request = request(
                "怀念", "距离", "故乡", "旧物", "时间", "离别");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tools.searchEmotionConcepts(request));

        assertEquals(ErrorCode.PARAM_ERROR, exception.getErrorCode());
    }

    @Test
    void shouldRejectKeywordOutsideLengthRange() {
        BusinessException shortKeyword = assertThrows(BusinessException.class,
                () -> tools.searchEmotionConcepts(request("怀")));
        BusinessException longKeyword = assertThrows(BusinessException.class,
                () -> tools.searchEmotionConcepts(request("这是一个超过二十个字符限制的情绪搜索关键词内容")));

        assertEquals(ErrorCode.PARAM_ERROR, shortKeyword.getErrorCode());
        assertEquals(ErrorCode.PARAM_ERROR, longKeyword.getErrorCode());
    }

    @Test
    void shouldNormalizeKeywordsAndUseFixedLimit() {
        EmotionConcept concept = concept(1L, "Sehnsucht");
        when(emotionConceptMapper.searchByKeywords(
                eq(Collections.singletonList("怀念")), eq(AppConstants.TOOL_QUERY_LIMIT)))
                .thenReturn(Collections.singletonList(concept));

        List<EmotionConceptToolItem> result = tools.searchEmotionConcepts(
                request("  怀念 ", "怀念", " ", null));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getConceptId());
        verify(emotionConceptMapper).searchByKeywords(
                Collections.singletonList("怀念"), AppConstants.TOOL_QUERY_LIMIT);
    }

    @Test
    void shouldAccumulateReturnedIdsAcrossToolCalls() {
        when(emotionConceptMapper.searchByKeywords(
                eq(Collections.singletonList("怀念")), eq(AppConstants.TOOL_QUERY_LIMIT)))
                .thenReturn(Collections.singletonList(concept(1L, "概念一")));
        when(emotionConceptMapper.searchByKeywords(
                eq(Collections.singletonList("宁静")), eq(AppConstants.TOOL_QUERY_LIMIT)))
                .thenReturn(Collections.singletonList(concept(2L, "概念二")));

        tools.searchEmotionConcepts(request("怀念"));
        tools.searchEmotionConcepts(request("宁静"));

        assertEquals(new java.util.HashSet<Long>(Arrays.asList(1L, 2L)),
                tools.getReturnedConceptIds());
        assertEquals(Arrays.asList(1L, 2L), Arrays.asList(
                tools.getReturnedConcepts().get(0).getConceptId(),
                tools.getReturnedConcepts().get(1).getConceptId()));
    }

    private EmotionConceptToolRequest request(String... keywords) {
        return EmotionConceptToolRequest.builder()
                .keywords(Arrays.asList(keywords))
                .build();
    }

    private EmotionConcept concept(Long id, String name) {
        return EmotionConcept.builder()
                .id(id)
                .name(name)
                .language("测试语言")
                .meaning("核心含义")
                .description("情境描述")
                .build();
    }
}
