package com.example.namedmoment.mapper;

import com.example.namedmoment.dto.ConceptCandidate;
import com.example.namedmoment.entity.EmotionConcept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmotionConceptMapper {

    EmotionConcept selectById(@Param("id") Long id);

    List<EmotionConcept> selectByIds(@Param("ids") List<Long> ids);

    List<ConceptCandidate> searchSimilar(@Param("queryVector") String queryVector,
                                         @Param("limit") int limit);

    List<EmotionConcept> searchByKeywords(@Param("keywords") List<String> keywords,
                                          @Param("limit") int limit);

    List<EmotionConcept> selectWithoutEmbedding(@Param("afterId") Long afterId,
                                                @Param("limit") int limit);

    int updateEmbedding(@Param("id") Long id, @Param("embedding") String embedding);
}
