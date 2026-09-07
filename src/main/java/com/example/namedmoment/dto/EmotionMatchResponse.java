package com.example.namedmoment.dto;

import com.example.namedmoment.enums.MatchMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionMatchResponse {

    private EmotionFingerprint fingerprint;
    private MatchMode matchMode;
    private List<EmotionMatchCandidateResponse> candidates;
}
