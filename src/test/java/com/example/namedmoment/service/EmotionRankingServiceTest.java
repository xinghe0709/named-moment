package com.example.namedmoment.service;

import com.example.namedmoment.dto.ConceptCandidate;
import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.ConceptRanking;
import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EmotionRankingServiceTest {

    private EmotionRankingService service;

    @BeforeEach
    void setUp() {
        service = spy(new EmotionRankingService());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
    }

    @Test
    void shouldValidateAndSortStructuredRanking() {
        doReturn(ranking(
                match(1L, 70),
                match(2L, 95),
                match(3L, 82)))
                .when(service).requestRanking(anyString());

        List<ConceptMatch> matches = service.rank(fingerprint(), candidates(1L, 2L, 3L));

        assertEquals(Arrays.asList(2L, 3L, 1L), Arrays.asList(
                matches.get(0).getConceptId(),
                matches.get(1).getConceptId(),
                matches.get(2).getConceptId()));
    }

    @Test
    void shouldRejectIdOutsideRetrievedCandidates() {
        doReturn(ranking(
                match(1L, 90),
                match(2L, 80),
                match(99L, 70)))
                .when(service).requestRanking(anyString());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.rank(fingerprint(), candidates(1L, 2L, 3L)));

        assertEquals(ErrorCode.CONCEPT_MATCH_FAILED, exception.getErrorCode());
    }

    @Test
    void shouldRetryWhenFirstRankingViolatesCandidateScope() {
        doReturn(ranking(match(1L, 90), match(2L, 80), match(99L, 70)),
                ranking(match(1L, 90), match(2L, 80), match(3L, 70)))
                .when(service).requestRanking(anyString());

        List<ConceptMatch> result = service.rank(
                fingerprint(), candidates(1L, 2L, 3L));

        assertEquals(Arrays.asList(1L, 2L, 3L), Arrays.asList(
                result.get(0).getConceptId(),
                result.get(1).getConceptId(),
                result.get(2).getConceptId()));
        verify(service, times(2)).requestRanking(anyString());
    }

    @Test
    void shouldRetryWhenExplanationNamesAnotherCandidate() {
        doReturn(ranking(
                        match(1L, 90, "概念2更贴合当前体验"),
                        match(2L, 80),
                        match(3L, 70)),
                ranking(match(1L, 90), match(2L, 80), match(3L, 70)))
                .when(service).requestRanking(anyString());

        List<ConceptMatch> result = service.rank(
                fingerprint(), candidates(1L, 2L, 3L));

        assertEquals(3, result.size());
        verify(service, times(2)).requestRanking(anyString());
    }

    @Test
    void shouldKeepRerankConstraintsInPrompt() throws Exception {
        ClassPathResource resource = new ClassPathResource("prompts/emotion-rerank.st");
        String prompt = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        assertTrue(prompt.contains("只能使用候选 conceptId"));
        assertTrue(prompt.contains("不得创造概念"));
        assertTrue(prompt.contains("返回三个结果"));
        assertTrue(prompt.contains("不要按知名度排序"));
    }

    private EmotionFingerprint fingerprint() {
        return EmotionFingerprint.builder()
                .meaning("对已经结束的生活仍有眷恋")
                .description("熟悉物品触发往昔记忆")
                .build();
    }

    private List<ConceptCandidate> candidates(Long first, Long second, Long third) {
        return Arrays.asList(candidate(first), candidate(second), candidate(third));
    }

    private ConceptCandidate candidate(Long id) {
        return ConceptCandidate.builder()
                .id(id)
                .name("概念" + id)
                .language("测试语言")
                .meaning("含义" + id)
                .description("描述" + id)
                .sourceUrl("https://example.test/" + id)
                .vectorScore(0.8D)
                .build();
    }

    private ConceptRanking ranking(ConceptMatch first, ConceptMatch second,
                                   ConceptMatch third) {
        return ConceptRanking.builder()
                .matches(Arrays.asList(first, second, third))
                .build();
    }

    private ConceptMatch match(Long id, Integer score) {
        return match(id, score, "匹配解释" + id);
    }

    private ConceptMatch match(Long id, Integer score, String explanation) {
        return ConceptMatch.builder()
                .conceptId(id)
                .matchScore(score)
                .explanation(explanation)
                .build();
    }
}
