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
import java.util.Map;
import java.util.Set;

public final class MatchResultUtils {

    private MatchResultUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<ConceptMatch> validateAndSort(List<ConceptMatch> matches,
                                                     Set<Long> allowedIds) {
        return validateAndSort(matches, allowedIds,
                Collections.<Long, String>emptyMap());
    }

    public static List<ConceptMatch> validateAndSort(List<ConceptMatch> matches,
                                                     Set<Long> allowedIds,
                                                     Map<Long, String> candidateNames) {
        if (matches == null || matches.size() != AppConstants.MATCH_RESULT_LIMIT
                || allowedIds == null || candidateNames == null) {
            throw matchFailure();
        }

        Set<Long> seenIds = new HashSet<Long>();
        for (ConceptMatch match : matches) {
            if (!isValid(match, allowedIds, seenIds, candidateNames)) {
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
                                   Set<Long> seenIds,
                                   Map<Long, String> candidateNames) {
        if (match == null || match.getConceptId() == null
                || !allowedIds.contains(match.getConceptId())
                || !seenIds.add(match.getConceptId())) {
            return false;
        }
        Integer score = match.getMatchScore();
        if (score == null || score < AppConstants.MATCH_SCORE_MIN
                || score > AppConstants.MATCH_SCORE_MAX) {
            return false;
        }
        String explanation = match.getExplanation();
        return explanation != null && !explanation.trim().isEmpty()
                && !referencesAnotherCandidate(
                match.getConceptId(), explanation, candidateNames);
    }

    private static boolean referencesAnotherCandidate(
            Long selectedConceptId,
            String explanation,
            Map<Long, String> candidateNames) {
        for (Map.Entry<Long, String> entry : candidateNames.entrySet()) {
            if (selectedConceptId.equals(entry.getKey())) {
                continue;
            }
            String candidateName = entry.getValue();
            if (candidateName != null && !candidateName.trim().isEmpty()
                    && explanation.contains(candidateName.trim())) {
                return true;
            }
        }
        return false;
    }

    private static BusinessException matchFailure() {
        return new BusinessException(ErrorCode.CONCEPT_MATCH_FAILED);
    }
}
