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
public class EmotionRecord {

    private Long id;
    private String inputText;
    private Long conceptId;
    private Integer matchScore;
    private String explanation;
    private OffsetDateTime createdAt;
}
