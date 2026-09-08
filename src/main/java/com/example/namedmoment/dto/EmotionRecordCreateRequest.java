package com.example.namedmoment.dto;

import com.example.namedmoment.constant.AppConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRecordCreateRequest {

    @NotBlank
    @Size(min = AppConstants.INPUT_MIN_LENGTH, max = AppConstants.INPUT_MAX_LENGTH)
    private String inputText;

    @NotNull
    @Positive
    private Long conceptId;

    @NotNull
    @Min(AppConstants.MATCH_SCORE_MIN)
    @Max(AppConstants.MATCH_SCORE_MAX)
    private Integer matchScore;

    @NotBlank
    private String explanation;
}
