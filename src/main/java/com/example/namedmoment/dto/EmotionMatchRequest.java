package com.example.namedmoment.dto;

import com.example.namedmoment.constant.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionMatchRequest {

    @NotBlank
    @Size(min = AppConstants.INPUT_MIN_LENGTH, max = AppConstants.INPUT_MAX_LENGTH)
    private String text;
}
