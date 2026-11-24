package com.medication.management.medication.dto.response;

import com.medication.management.medication.enums.MedicationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PersonalMedicationResponseDto(
        UUID id,
        String name,
        String dosage,
        String instructions,
        LocalDate startDate,
        LocalDate endDate,
        MedicationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
