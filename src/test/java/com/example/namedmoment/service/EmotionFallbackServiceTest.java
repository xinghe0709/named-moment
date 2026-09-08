package com.example.namedmoment.service;

import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.ConceptRanking;
import com.example.namedmoment.dto.EmotionConceptToolRequest;
import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import com.example.namedmoment.tool.EmotionConceptTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.FileCopyUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmotionFallbackServiceTest {

    private EmotionFallbackService service;
    private EmotionConceptTools tool;

    @BeforeEach
    void setUp() {
        EmotionConceptMapper mapper = mock(EmotionConceptMapper.class);
        when(mapper.searchByKeywords(eq(Arrays.asList("怀念", "旧物")), eq(20)))
                .thenReturn(Arrays.asList(concept(1L), concept(2L), concept(3L)));

        tool = new EmotionConceptTools();
        ReflectionTestUtils.setField(tool, "emotionConceptMapper", mapper);
        tool.searchEmotionConcepts(EmotionConceptToolRequest.builder()
                .keywords(Arrays.asList("怀念", "旧物"))
                .build());

        @SuppressWarnings("unchecked")
        ObjectProvider<EmotionConceptTools> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(tool);

        service = spy(new EmotionFallbackService());
        ReflectionTestUtils.setField(service, "emotionConceptToolsProvider", provider);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        doNothing().when(service).requestToolSearch(anyString(), eq(tool));
    }

    @Test
    void shouldAcceptOnlyIdsReturnedByTool() {
        doReturn(ranking(1L, 2L, 3L))
                .when(service).requestRanking(anyString());

        List<ConceptMatch> result = service.match(fingerprint());

        assertEquals(Arrays.asList(1L, 2L, 3L), Arrays.asList(
                result.get(0).getConceptId(),
                result.get(1).getConceptId(),
                result.get(2).getConceptId()));
    }

    @Test
    void shouldRejectIdNotReturnedByTool() {
        doReturn(ranking(1L, 2L, 99L))
                .when(service).requestRanking(anyString());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.match(fingerprint()));

        assertEquals(ErrorCode.CONCEPT_MATCH_FAILED, exception.getErrorCode());
    }

    @Test
    void shouldRetryWhenFirstToolRankingIsInvalid() {
        doReturn(ranking(1L, 2L, 99L), ranking(1L, 2L, 3L))
                .when(service).requestRanking(anyString());

        List<ConceptMatch> result = service.match(fingerprint());

        assertEquals(Arrays.asList(1L, 2L, 3L), Arrays.asList(
                result.get(0).getConceptId(),
                result.get(1).getConceptId(),
                result.get(2).getConceptId()));
        verify(service, times(2)).requestRanking(anyString());
    }

    @Test
    void shouldRetryWhenToolExplanationNamesAnotherCandidate() {
        doReturn(ConceptRanking.builder()
                        .matches(Arrays.asList(
                                match(1L, 93, "概念2更贴合当前体验"),
                                match(2L, 88),
                                match(3L, 80)))
                        .build(),
                ranking(1L, 2L, 3L))
                .when(service).requestRanking(anyString());

        List<ConceptMatch> result = service.match(fingerprint());

        assertEquals(3, result.size());
        verify(service, times(2)).requestRanking(anyString());
    }

    @Test
    void shouldPreserveDatabaseFailure() {
        doThrow(new DataAccessResourceFailureException("database unavailable"))
                .when(service).requestToolSearch(anyString(), eq(tool));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.match(fingerprint()));

        assertEquals(ErrorCode.DATABASE_ERROR, exception.getErrorCode());
    }

    @Test
    void shouldKeepToolFallbackConstraintsInPrompt() throws Exception {
        ClassPathResource resource = new ClassPathResource("prompts/emotion-fallback.st");
        String prompt = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        assertTrue(prompt.contains("必须调用一次 searchEmotionConcepts"));
        assertTrue(prompt.contains("只负责选择检索关键词并调用工具"));

        ClassPathResource rerankResource =
                new ClassPathResource("prompts/emotion-tool-rerank.st");
        String rerankPrompt = FileCopyUtils.copyToString(
                new InputStreamReader(rerankResource.getInputStream(), StandardCharsets.UTF_8));
        assertTrue(rerankPrompt.contains("只能使用候选 conceptId"));
        assertTrue(rerankPrompt.contains("不得创造概念"));
        assertTrue(rerankPrompt.contains("返回三个结果"));
    }

    private EmotionFingerprint fingerprint() {
        return EmotionFingerprint.builder()
                .meaning("对离开的生活仍有眷恋")
                .description("熟悉旧物突然唤起从前")
                .build();
    }

    private ConceptRanking ranking(Long first, Long second, Long third) {
        return ConceptRanking.builder()
                .matches(Arrays.asList(
                        match(first, 93),
                        match(second, 88),
                        match(third, 80)))
                .build();
    }

    private ConceptMatch match(Long id, Integer score) {
        return match(id, score, "工具兜底解释" + id);
    }

    private ConceptMatch match(Long id, Integer score, String explanation) {
        return ConceptMatch.builder()
                .conceptId(id)
                .matchScore(score)
                .explanation(explanation)
                .build();
    }

    private EmotionConcept concept(Long id) {
        return EmotionConcept.builder()
                .id(id)
                .name("概念" + id)
                .language("测试语言")
                .meaning("含义" + id)
                .description("描述" + id)
                .build();
    }
}
