package com.example.namedmoment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptCandidate {

    private Long id;
    private String name;
    private String language;
    private String meaning;
    private String description;
    private String sourceUrl;
    private Double vectorScore;
}
