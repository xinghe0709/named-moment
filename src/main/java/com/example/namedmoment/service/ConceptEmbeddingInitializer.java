package com.example.namedmoment.service;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import com.example.namedmoment.utils.EmbeddingTextUtils;
import com.example.namedmoment.utils.ExceptionLogUtils;
import com.example.namedmoment.utils.VectorUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ConceptEmbeddingInitializer implements ApplicationRunner {

    @Resource
    private EmotionConceptMapper emotionConceptMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    @Value("${AI_API_KEY:}")
    private String aiApiKey;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(aiApiKey)) {
            log.warn("stage=embedding-init status=skipped reason=missing-api-key "
                    + "action=configure-project-dotenv");
            return;
        }

        long startedAt = System.nanoTime();
        int processedCount = 0;
        int failedCount = 0;
        log.info("stage=embedding-init status=started batchSize={}",
                AppConstants.EMBEDDING_BATCH_SIZE);
        long afterId = 0L;
        while (true) {
            List<EmotionConcept> batch = emotionConceptMapper.selectWithoutEmbedding(
                    afterId, AppConstants.EMBEDDING_BATCH_SIZE);
            if (batch.isEmpty()) {
                log.info("stage=embedding-init status=complete processedCount={} "
                                + "failedCount={} durationMs={}",
                        processedCount, failedCount,
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt));
                return;
            }

            for (EmotionConcept concept : batch) {
                processedCount++;
                if (!initializeConcept(concept)) {
                    failedCount++;
                }
            }
            afterId = batch.get(batch.size() - 1).getId();
        }
    }

    private boolean initializeConcept(EmotionConcept concept) {
        try {
            String text = EmbeddingTextUtils.build(
                    concept.getMeaning(), concept.getDescription());
            float[] embedding = embeddingModel.embed(text);
            emotionConceptMapper.updateEmbedding(
                    concept.getId(), VectorUtils.toPgVector(embedding));
            return true;
        } catch (Exception exception) {
            log.warn("stage=embedding-init conceptId={} status=failed type={} "
                            + "rootType={} rootMessage={}",
                    concept.getId(), exception.getClass().getSimpleName(),
                    ExceptionLogUtils.rootType(exception),
                    ExceptionLogUtils.rootMessage(exception));
            return false;
        }
    }
}
