package com.medication.management.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(
        @Pattern(regexp = ".*\\S.*", message = "First name cannot be blank")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @Pattern(regexp = ".*\\S.*", message = "Last name cannot be blank")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @Pattern(regexp = ".*\\S.*", message = "Timezone cannot be blank")
        @Size(max = 64, message = "Timezone cannot exceed 64 characters")
        String timezone
) {
}
