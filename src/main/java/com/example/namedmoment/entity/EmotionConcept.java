package com.example.namedmoment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionConcept {

    private Long id;
    private String name;
    private String language;
    private String meaning;
    private String description;
    private String sourceUrl;
    private String embedding;
    private OffsetDateTime createdAt;
}
