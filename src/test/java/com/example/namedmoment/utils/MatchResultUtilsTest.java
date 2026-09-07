package com.example.namedmoment.utils;

import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MatchResultUtilsTest {

    private final Set<Long> allowedIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));

    @Test
    void shouldSortValidMatchesByScore() {
        List<ConceptMatch> result = MatchResultUtils.validateAndSort(Arrays.asList(
                match(1L, 70, "第一条解释"),
                match(2L, 95, "第二条解释"),
                match(3L, 82, "第三条解释")
        ), allowedIds);

        assertEquals(Arrays.asList(2L, 3L, 1L), Arrays.asList(
                result.get(0).getConceptId(),
                result.get(1).getConceptId(),
                result.get(2).getConceptId()));
    }

    @Test
    void shouldRejectUnknownConceptId() {
        assertMatchFailure(Arrays.asList(
                match(1L, 90, "解释一"),
                match(2L, 80, "解释二"),
                match(99L, 70, "解释三")
        ));
    }

    @Test
    void shouldRejectDuplicateConceptId() {
        assertMatchFailure(Arrays.asList(
                match(1L, 90, "解释一"),
                match(1L, 80, "解释二"),
                match(3L, 70, "解释三")
        ));
    }

    @Test
    void shouldRejectWrongResultCount() {
        assertMatchFailure(Arrays.asList(
                match(1L, 90, "解释一"),
                match(2L, 80, "解释二")
        ));
    }

    @Test
    void shouldRejectScoreOutsideRange() {
        assertMatchFailure(Arrays.asList(
                match(1L, 101, "解释一"),
                match(2L, 80, "解释二"),
                match(3L, 70, "解释三")
        ));
    }

    @Test
    void shouldRejectBlankExplanation() {
        assertMatchFailure(Arrays.asList(
                match(1L, 90, "解释一"),
                match(2L, 80, " "),
                match(3L, 70, "解释三")
        ));
    }

    private void assertMatchFailure(List<ConceptMatch> matches) {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> MatchResultUtils.validateAndSort(matches, allowedIds));
        assertEquals(ErrorCode.CONCEPT_MATCH_FAILED, exception.getErrorCode());
    }

    private ConceptMatch match(Long conceptId, Integer score, String explanation) {
        return ConceptMatch.builder()
                .conceptId(conceptId)
                .matchScore(score)
                .explanation(explanation)
                .build();
    }
}
