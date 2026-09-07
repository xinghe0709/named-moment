package com.example.namedmoment.service;

import com.example.namedmoment.constant.AppConstants;
import com.example.namedmoment.entity.EmotionConcept;
import com.example.namedmoment.mapper.EmotionConceptMapper;
import com.example.namedmoment.utils.EmbeddingTextUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptEmbeddingInitializerTest {

    @Mock
    private EmotionConceptMapper emotionConceptMapper;

    @Mock
    private EmbeddingModel embeddingModel;

    private ConceptEmbeddingInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new ConceptEmbeddingInitializer();
        ReflectionTestUtils.setField(initializer, "emotionConceptMapper", emotionConceptMapper);
        ReflectionTestUtils.setField(initializer, "embeddingModel", embeddingModel);
    }

    @Test
    void shouldAdvanceCursorWhenOneConceptFails() throws Exception {
        EmotionConcept first = concept(1L, "第一种感受", "第一种情境");
        EmotionConcept second = concept(2L, "第二种感受", "第二种情境");
        when(emotionConceptMapper.selectWithoutEmbedding(0L, AppConstants.EMBEDDING_BATCH_SIZE))
                .thenReturn(Arrays.asList(first, second));
        when(emotionConceptMapper.selectWithoutEmbedding(2L, AppConstants.EMBEDDING_BATCH_SIZE))
                .thenReturn(Collections.<EmotionConcept>emptyList());
        when(embeddingModel.embed(EmbeddingTextUtils.build(first.getMeaning(), first.getDescription())))
                .thenReturn(new float[AppConstants.EMBEDDING_DIMENSION]);
        when(embeddingModel.embed(EmbeddingTextUtils.build(second.getMeaning(), second.getDescription())))
                .thenThrow(new RuntimeException("provider unavailable"));

        initializer.run(null);

        verify(emotionConceptMapper).updateEmbedding(org.mockito.ArgumentMatchers.eq(1L), anyString());
        verify(emotionConceptMapper, never())
                .updateEmbedding(org.mockito.ArgumentMatchers.eq(2L), anyString());
        verify(emotionConceptMapper)
                .selectWithoutEmbedding(2L, AppConstants.EMBEDDING_BATCH_SIZE);
    }

    @Test
    void shouldSkipWrongDimensionVector() throws Exception {
        EmotionConcept concept = concept(1L, "一种感受", "一种情境");
        when(emotionConceptMapper.selectWithoutEmbedding(0L, AppConstants.EMBEDDING_BATCH_SIZE))
                .thenReturn(Collections.singletonList(concept));
        when(emotionConceptMapper.selectWithoutEmbedding(1L, AppConstants.EMBEDDING_BATCH_SIZE))
                .thenReturn(Collections.<EmotionConcept>emptyList());
        when(embeddingModel.embed(anyString())).thenReturn(new float[511]);

        initializer.run(null);

        verify(emotionConceptMapper, never()).updateEmbedding(
                org.mockito.ArgumentMatchers.anyLong(), anyString());
    }

    private EmotionConcept concept(Long id, String meaning, String description) {
        return EmotionConcept.builder()
                .id(id)
                .meaning(meaning)
                .description(description)
                .build();
    }
}
