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
public class AppUser {

    private Long id;
    private String username;
    private String passwordHash;
    private OffsetDateTime createdAt;
}
