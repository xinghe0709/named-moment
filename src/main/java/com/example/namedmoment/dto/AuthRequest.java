package com.example.namedmoment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank
    @Size(min = 3, max = 32)
    @Pattern(regexp = "[A-Za-z0-9_-]+")
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
