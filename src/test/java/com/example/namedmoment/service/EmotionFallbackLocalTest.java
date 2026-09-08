package com.example.namedmoment.service;

import com.example.namedmoment.dto.ConceptMatch;
import com.example.namedmoment.dto.EmotionFingerprint;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "logging.config=classpath:logback-local-ai.xml")
@EnabledIfEnvironmentVariable(named = "RUN_LOCAL_AI_IT", matches = "true")
class EmotionFallbackLocalTest {

    @Resource
    private EmotionFallbackService emotionFallbackService;

    @Test
    void shouldCallDatabaseToolAndReturnThreeConcepts() {
        EmotionFingerprint fingerprint = EmotionFingerprint.builder()
                .meaning("熟悉旧物突然唤起对回不去生活的怀念")
                .description("毕业离开旧城市后，一瓶熟悉饮料让从前显得遥远而真实。")
                .build();

        List<ConceptMatch> matches = emotionFallbackService.match(fingerprint);

        assertEquals(3, matches.size());
        HashSet<Long> conceptIds = new HashSet<Long>();
        for (ConceptMatch match : matches) {
            conceptIds.add(match.getConceptId());
        }
        assertEquals(3, conceptIds.size());
    }
}
