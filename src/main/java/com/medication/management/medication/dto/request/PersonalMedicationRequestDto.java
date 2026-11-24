package com.medication.management.medication.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PersonalMedicationRequestDto(

        @NotBlank(message = "Medication name is required")
        @Size(max = 150, message = "Medication name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "Dosage is required")
        @Size(max = 100, message = "Dosage cannot exceed 100 characters")
        String dosage,

        @Size(max = 1000, message = "Instructions cannot exceed 1000 characters")
        String instructions,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate

) {

    @AssertTrue(message = "End date cannot be before start date")
    public boolean isDateRangeValid() {
        return startDate == null
                || endDate == null
                || !endDate.isBefore(startDate);
    }
}