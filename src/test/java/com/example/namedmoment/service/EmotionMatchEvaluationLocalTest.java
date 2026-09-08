package com.example.namedmoment.service;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.dto.EmotionMatchCandidateResponse;
import com.example.namedmoment.dto.EmotionMatchResponse;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "logging.config=classpath:logback-local-ai.xml")
@EnabledIfEnvironmentVariable(named = "RUN_LOCAL_AI_EVAL", matches = "true")
class EmotionMatchEvaluationLocalTest {

    private static final int EXPECTED_CASE_COUNT = 20;
    private static final int MIN_ACCEPTABLE_HITS = 16;

    @Resource
    private EmotionMatchService emotionMatchService;

    @Resource
    private EmotionConceptMapper emotionConceptMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    void shouldMeetMvpQualityGateAcrossTwentyCases() throws Exception {
        List<EvaluationCase> cases = loadCases();
        assertEquals(EXPECTED_CASE_COUNT, cases.size());

        List<EvaluationResult> results = new ArrayList<EvaluationResult>();
        List<String> hardFailures = new ArrayList<String>();
        List<String> acceptableMisses = new ArrayList<String>();
        int acceptableHitCount = 0;

        for (EvaluationCase evaluationCase : cases) {
            EvaluationResult result = evaluate(
                    evaluationCase, hardFailures, acceptableMisses);
            results.add(result);
            if (result.isAcceptableHit()) {
                acceptableHitCount++;
            }
        }

        if (acceptableHitCount < MIN_ACCEPTABLE_HITS) {
            hardFailures.add("Top 3 acceptable hits were " + acceptableHitCount
                    + "/" + EXPECTED_CASE_COUNT + ", expected at least "
                    + MIN_ACCEPTABLE_HITS + "; misses: " + acceptableMisses);
        }

        writeReport(results, acceptableHitCount, acceptableMisses);
        assertTrue(hardFailures.isEmpty(),
                String.join(System.lineSeparator(), hardFailures));
    }

    private EvaluationResult evaluate(EvaluationCase evaluationCase,
                                      List<String> hardFailures,
                                      List<String> acceptableMisses) {
        EvaluationResult result = new EvaluationResult();
        result.setId(evaluationCase.getId());
        result.setInput(evaluationCase.getInput());
        result.setAcceptableConcepts(evaluationCase.getAcceptableConcepts());

        int failureCountBefore = hardFailures.size();
        long startedAt = System.nanoTime();
        try {
            EmotionMatchResponse response = emotionMatchService.match(evaluationCase.getInput());
            result.setResponse(response);
            validateResponse(evaluationCase, response, hardFailures);
            result.setStructuralPass(hardFailures.size() == failureCountBefore);
            result.setAcceptableHit(containsAcceptableConcept(evaluationCase, response));
            if (!result.isAcceptableHit()) {
                acceptableMisses.add(casePrefix(evaluationCase)
                        + "Top 3 did not contain an acceptable concept: "
                        + candidateNames(response));
            }
        } catch (Exception exception) {
            result.setError(exception.getClass().getSimpleName());
            result.setStructuralPass(false);
            hardFailures.add(casePrefix(evaluationCase)
                    + "request failed with " + exception.getClass().getSimpleName());
        }
        result.setLatencyMs((System.nanoTime() - startedAt) / 1_000_000L);
        return result;
    }

    private void validateResponse(EvaluationCase evaluationCase,
                                  EmotionMatchResponse response,
                                  List<String> failures) {
        if (response == null || response.getCandidates() == null) {
            failures.add(casePrefix(evaluationCase) + "response or candidates were null");
            return;
        }
        List<EmotionMatchCandidateResponse> candidates = response.getCandidates();
        if (candidates.size() != AppConstants.MATCH_RESULT_LIMIT) {
            failures.add(casePrefix(evaluationCase) + "candidate count was "
                    + candidates.size());
        }

        Set<Long> conceptIds = new HashSet<Long>();
        Integer previousScore = null;
        for (EmotionMatchCandidateResponse candidate : candidates) {
            conceptIds.add(candidate.getConceptId());
            validateDatabaseFacts(evaluationCase, candidate, failures);
            if (candidate.getMatchScore() == null
                    || candidate.getMatchScore() < AppConstants.MATCH_SCORE_MIN
                    || candidate.getMatchScore() > AppConstants.MATCH_SCORE_MAX) {
                failures.add(casePrefix(evaluationCase) + "invalid score for "
                        + candidate.getName());
            }
            if (previousScore != null && candidate.getMatchScore() != null
                    && previousScore < candidate.getMatchScore()) {
                failures.add(casePrefix(evaluationCase) + "scores were not descending");
            }
            previousScore = candidate.getMatchScore();
            if (candidate.getExplanation() == null
                    || candidate.getExplanation().trim().isEmpty()) {
                failures.add(casePrefix(evaluationCase) + "empty explanation for "
                        + candidate.getName());
            }
        }
        if (conceptIds.size() != candidates.size()) {
            failures.add(casePrefix(evaluationCase) + "contained duplicate concept IDs");
        }
    }

    private void validateDatabaseFacts(EvaluationCase evaluationCase,
                                       EmotionMatchCandidateResponse candidate,
                                       List<String> failures) {
        EmotionConcept concept = emotionConceptMapper.selectById(candidate.getConceptId());
        if (concept == null) {
            failures.add(casePrefix(evaluationCase) + "unknown concept ID "
                    + candidate.getConceptId());
            return;
        }
        if (!same(concept.getName(), candidate.getName())
                || !same(concept.getLanguage(), candidate.getLanguage())
                || !same(concept.getMeaning(), candidate.getMeaning())
                || !same(concept.getDescription(), candidate.getDescription())
                || !same(concept.getSourceUrl(), candidate.getSourceUrl())) {
            failures.add(casePrefix(evaluationCase) + "database facts changed for "
                    + candidate.getConceptId());
        }
        if (candidate.getSourceUrl() == null
                || !candidate.getSourceUrl().startsWith("https://")) {
            failures.add(casePrefix(evaluationCase) + "source was not HTTPS for "
                    + candidate.getName());
        }
    }

    private boolean containsAcceptableConcept(EvaluationCase evaluationCase,
                                              EmotionMatchResponse response) {
        if (response == null || response.getCandidates() == null) {
            return false;
        }
        Set<String> acceptable = new HashSet<String>(evaluationCase.getAcceptableConcepts());
        for (EmotionMatchCandidateResponse candidate : response.getCandidates()) {
            if (acceptable.contains(candidate.getName())) {
                return true;
            }
        }
        return false;
    }

    private List<String> candidateNames(EmotionMatchResponse response) {
        List<String> names = new ArrayList<String>();
        for (EmotionMatchCandidateResponse candidate : response.getCandidates()) {
            names.add(candidate.getName());
        }
        return names;
    }

    private List<EvaluationCase> loadCases() throws Exception {
        ClassPathResource resource = new ClassPathResource(
                "evaluation/emotion-match-cases.json");
        return objectMapper.readValue(resource.getInputStream(),
                new TypeReference<List<EvaluationCase>>() { });
    }

    private void writeReport(List<EvaluationResult> results,
                             int acceptableHitCount,
                             List<String> acceptableMisses) throws Exception {
        Map<String, Object> report = new LinkedHashMap<String, Object>();
        report.put("caseCount", results.size());
        report.put("acceptableHitCount", acceptableHitCount);
        report.put("minimumAcceptableHits", MIN_ACCEPTABLE_HITS);
        report.put("acceptableMisses", acceptableMisses);
        report.put("results", results);

        Path targetDirectory = Paths.get("target");
        Files.createDirectories(targetDirectory);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                targetDirectory.resolve("emotion-evaluation-results.json").toFile(),
                report);
    }

    private boolean same(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private String casePrefix(EvaluationCase evaluationCase) {
        return "case " + evaluationCase.getId() + ": ";
    }

    @Data
    public static class EvaluationCase {
        private int id;
        private String input;
        private List<String> acceptableConcepts;
    }

    @Data
    public static class EvaluationResult {
        private int id;
        private String input;
        private List<String> acceptableConcepts;
        private EmotionMatchResponse response;
        private long latencyMs;
        private boolean structuralPass;
        private boolean acceptableHit;
        private String error;
    }
}
