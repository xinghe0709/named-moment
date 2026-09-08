package com.example.namedmoment.service;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import com.example.namedmoment.utils.EmbeddingTextUtils;
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

@Slf4j
@Component
public class ConceptEmbeddingInitializer implements ApplicationRunner {

    @Resource
    private EmotionConceptMapper emotionConceptMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    @Value("${DASHSCOPE_API_KEY:}")
    private String dashScopeApiKey;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(dashScopeApiKey)) {
            log.warn("stage=embedding-init status=skipped reason=missing-api-key");
            return;
        }

        long afterId = 0L;
        while (true) {
            List<EmotionConcept> batch = emotionConceptMapper.selectWithoutEmbedding(
                    afterId, AppConstants.EMBEDDING_BATCH_SIZE);
            if (batch.isEmpty()) {
                return;
            }

            for (EmotionConcept concept : batch) {
                initializeConcept(concept);
            }
            afterId = batch.get(batch.size() - 1).getId();
        }
    }

    private void initializeConcept(EmotionConcept concept) {
        try {
            String text = EmbeddingTextUtils.build(
                    concept.getMeaning(), concept.getDescription());
            float[] embedding = embeddingModel.embed(text);
            emotionConceptMapper.updateEmbedding(
                    concept.getId(), VectorUtils.toPgVector(embedding));
        } catch (Exception exception) {
            log.warn("stage=embedding-init conceptId={} status=failed type={}",
                    concept.getId(), exception.getClass().getSimpleName());
        }
    }
}
