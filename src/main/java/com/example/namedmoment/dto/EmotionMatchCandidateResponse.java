package com.example.namedmoment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionMatchCandidateResponse {

    private Long conceptId;
    private String name;
    private String language;
    private String meaning;
    private String description;
    private String sourceUrl;
    private Integer matchScore;
    private String explanation;
}
