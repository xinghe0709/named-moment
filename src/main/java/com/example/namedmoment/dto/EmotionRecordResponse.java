package com.example.namedmoment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRecordResponse {

    private Long id;
    private String inputText;
    private Long conceptId;
    private String name;
    private String language;
    private String meaning;
    private String description;
    private String sourceUrl;
    private Integer matchScore;
    private String explanation;
    private OffsetDateTime createdAt;
}
