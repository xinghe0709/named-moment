package com.example.namedmoment.service;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.dto.ConceptCandidate;
import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.EmotionFingerprint;
import com.example.namedmoment.dto.EmotionMatchResponse;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.enums.MatchMode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmotionMatchServiceTest {

    @Mock private EmotionFingerprintService emotionFingerprintService;
    @Mock private EmotionRankingService emotionRankingService;
    @Mock private EmotionFallbackService emotionFallbackService;
    @Mock private EmotionConceptMapper emotionConceptMapper;
    @Mock private EmbeddingModel embeddingModel;

    private EmotionMatchService service;
    private EmotionFingerprint fingerprint;

    @BeforeEach
    void setUp() {
        service = new EmotionMatchService();
        ReflectionTestUtils.setField(service, "emotionFingerprintService", emotionFingerprintService);
        ReflectionTestUtils.setField(service, "emotionRankingService", emotionRankingService);
        ReflectionTestUtils.setField(service, "emotionFallbackService", emotionFallbackService);
        ReflectionTestUtils.setField(service, "emotionConceptMapper", emotionConceptMapper);
        ReflectionTestUtils.setField(service, "embeddingModel", embeddingModel);

        fingerprint = EmotionFingerprint.builder()
                .meaning("对结束的异乡生活仍有眷恋")
                .description("旧物突然唤起过去，仿佛看见另一个世界里的自己")
                .build();
    }

    @Test
    void shouldReturnRagResultsWithoutFallback() {
        arrangeFingerprintAndRag(candidates(0.91D, 0.85D, 0.72D));
        when(emotionRankingService.rank(eq(fingerprint), anyList()))
                .thenReturn(matches(1L, 2L, 3L));
        arrangeConceptLookup(1L, 2L, 3L);

        EmotionMatchResponse response = service.match("一段足够长的异乡回忆");

        assertEquals(MatchMode.RAG, response.getMatchMode());
        assertEquals(3, response.getCandidates().size());
        assertEquals("概念1", response.getCandidates().get(0).getName());
        verify(emotionFallbackService, never()).match(fingerprint);
    }

    @Test
    void shouldFallbackWhenEmbeddingFails() {
        when(emotionFingerprintService.analyze(anyString())).thenReturn(fingerprint);
        when(embeddingModel.embed(anyString())).thenThrow(new RuntimeException("embedding unavailable"));
        when(emotionFallbackService.match(fingerprint)).thenReturn(matches(1L, 2L, 3L));
        arrangeConceptLookup(1L, 2L, 3L);

        EmotionMatchResponse response = service.match("一段足够长的异乡回忆");

        assertEquals(MatchMode.TOOL_FALLBACK, response.getMatchMode());
        verify(emotionFallbackService).match(fingerprint);
    }

    @Test
    void shouldFallbackWhenRerankRejectsIds() {
        arrangeFingerprintAndRag(candidates(0.91D, 0.85D, 0.72D));
        when(emotionRankingService.rank(eq(fingerprint), anyList()))
                .thenThrow(new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED));
        when(emotionFallbackService.match(fingerprint)).thenReturn(matches(1L, 2L, 3L));
        arrangeConceptLookup(1L, 2L, 3L);

        EmotionMatchResponse response = service.match("一段足够长的异乡回忆");

        assertEquals(MatchMode.TOOL_FALLBACK, response.getMatchMode());
    }

    @Test
    void shouldFailWhenRagAndFallbackBothFail() {
        when(emotionFingerprintService.analyze(anyString())).thenReturn(fingerprint);
        when(embeddingModel.embed(anyString())).thenThrow(new RuntimeException("embedding unavailable"));
        when(emotionFallbackService.match(fingerprint))
                .thenThrow(new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.match("一段足够长的异乡回忆"));

        assertEquals(ErrorCode.CONCEPT_MATCH_FAILED, exception.getErrorCode());
    }

    @Test
    void shouldNotFallbackWhenFingerprintFails() {
        when(emotionFingerprintService.analyze(anyString()))
                .thenThrow(new BusinessException(ErrorCode.FINGERPRINT_FAILED));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.match("一段足够长的异乡回忆"));

        assertEquals(ErrorCode.FINGERPRINT_FAILED, exception.getErrorCode());
        verify(emotionFallbackService, never()).match(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldFilterWeakVectorCandidatesBeforeRerank() {
        List<ConceptCandidate> candidates = candidates(0.91D, 0.85D, 0.72D, 0.49D);
        arrangeFingerprintAndRag(candidates);
        when(emotionRankingService.rank(eq(fingerprint), anyList()))
                .thenReturn(matches(1L, 2L, 3L));
        arrangeConceptLookup(1L, 2L, 3L);

        service.match("一段足够长的异乡回忆");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ConceptCandidate>> captor = ArgumentCaptor.forClass(List.class);
        verify(emotionRankingService).rank(eq(fingerprint), captor.capture());
        assertEquals(3, captor.getValue().size());
    }

    private void arrangeFingerprintAndRag(List<ConceptCandidate> candidates) {
        when(emotionFingerprintService.analyze(anyString())).thenReturn(fingerprint);
        when(embeddingModel.embed(anyString()))
                .thenReturn(new float[AppConstants.EMBEDDING_DIMENSION]);
        when(emotionConceptMapper.searchSimilar(anyString(),
                eq(AppConstants.VECTOR_RECALL_LIMIT))).thenReturn(candidates);
    }

    private void arrangeConceptLookup(Long... ids) {
        List<EmotionConcept> concepts = new ArrayList<EmotionConcept>();
        for (Long id : ids) {
            concepts.add(EmotionConcept.builder()
                    .id(id)
                    .name("概念" + id)
                    .language("测试语言")
                    .meaning("含义" + id)
                    .description("描述" + id)
                    .sourceUrl("https://example.test/" + id)
                    .build());
        }
        when(emotionConceptMapper.selectByIds(anyList())).thenReturn(concepts);
    }

    private List<ConceptCandidate> candidates(Double... scores) {
        List<ConceptCandidate> candidates = new ArrayList<ConceptCandidate>();
        for (int index = 0; index < scores.length; index++) {
            long id = index + 1L;
            candidates.add(ConceptCandidate.builder()
                    .id(id)
                    .name("候选" + id)
                    .language("测试语言")
                    .meaning("含义" + id)
                    .description("描述" + id)
                    .sourceUrl("https://example.test/" + id)
                    .vectorScore(scores[index])
                    .build());
        }
        return candidates;
    }

    private List<ConceptMatch> matches(Long first, Long second, Long third) {
        return Arrays.asList(
                match(first, 95),
                match(second, 88),
                match(third, 80));
    }

    private ConceptMatch match(Long id, Integer score) {
        return ConceptMatch.builder()
                .conceptId(id)
                .matchScore(score)
                .explanation("匹配解释" + id)
                .build();
    }
}
