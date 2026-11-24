package com.medication.management.user.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDto(UUID id,
                              String email,
                              String firstName,
                              String lastName,
                              String timezone,
                              Instant createdAt,
                              Instant updatedAt) {
}
