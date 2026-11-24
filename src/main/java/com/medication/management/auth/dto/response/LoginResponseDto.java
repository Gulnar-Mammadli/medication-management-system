package com.medication.management.auth.dto.response;

public record LoginResponseDto(
        String token,
        String tokenType,
        long expiresIn
) {
}
