package com.example.namedmoment.utils;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MatchResultUtils {

    private MatchResultUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<ConceptMatch> validateAndSort(List<ConceptMatch> matches,
                                                     Set<Long> allowedIds) {
        if (matches == null || matches.size() != AppConstants.MATCH_RESULT_LIMIT
                || allowedIds == null) {
            throw matchFailure();
        }

        Set<Long> seenIds = new HashSet<Long>();
        for (ConceptMatch match : matches) {
            if (!isValid(match, allowedIds, seenIds)) {
                throw matchFailure();
            }
        }

        List<ConceptMatch> sorted = new ArrayList<ConceptMatch>(matches);
        Collections.sort(sorted, new Comparator<ConceptMatch>() {
            @Override
            public int compare(ConceptMatch left, ConceptMatch right) {
                return right.getMatchScore().compareTo(left.getMatchScore());
            }
        });
        return sorted;
    }

    private static boolean isValid(ConceptMatch match, Set<Long> allowedIds,
                                   Set<Long> seenIds) {
        if (match == null || match.getConceptId() == null
                || !allowedIds.contains(match.getConceptId())
                || !seenIds.add(match.getConceptId())) {
            return false;
        }
        Integer score = match.getMatchScore();
        if (score == null || score < 0 || score > 100) {
            return false;
        }
        String explanation = match.getExplanation();
        return explanation != null && !explanation.trim().isEmpty();
    }

    private static BusinessException matchFailure() {
        return new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);
    }
}
