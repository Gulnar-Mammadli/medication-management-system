package com.medication.management.medication.controller;

import com.medication.management.auth.security.CustomUserDetails;
import com.medication.management.common.exception.InvalidRequestException;
import com.medication.management.medication.dto.request.PersonalMedicationRequestDto;
import com.medication.management.medication.dto.response.PersonalMedicationResponseDto;
import com.medication.management.medication.enums.MedicationStatus;
import com.medication.management.medication.service.PersonalMedicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
public class PersonalMedicationController {

    private final PersonalMedicationService medicationService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "startDate",
            "endDate",
            "createdAt",
            "updatedAt"
    );

    // Allow sorting by certain fields
    private Pageable validatePageable(Pageable pageable) {

        pageable.getSort().forEach(order -> {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException(
                        "Sorting by '" + order.getProperty() + "' is not allowed"
                );
            }
        });

        return pageable;
    }

    @PostMapping
    public ResponseEntity<PersonalMedicationResponseDto> createMedication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PersonalMedicationRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(medicationService.createMedication(
                        userDetails.id(),
                        request
                ));
    }

    @GetMapping
    public ResponseEntity<Page<PersonalMedicationResponseDto>> getAllMedications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) MedicationStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                medicationService.getAllMedications(
                        userDetails.id(),
                        status,
                        validatePageable(pageable)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonalMedicationResponseDto> getMedicationById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                medicationService.getMedicationById(
                        userDetails.id(),
                        id
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonalMedicationResponseDto> updateMedication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody PersonalMedicationRequestDto request
    ) {
        return ResponseEntity.ok(
                medicationService.updateMedication(
                        userDetails.id(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        medicationService.deleteMedication(userDetails.id(), id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PersonalMedicationResponseDto> updateMedicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam MedicationStatus status
    ) {
        return ResponseEntity.ok(
                medicationService.updateMedicationStatus(
                        userDetails.id(),
                        id,
                        status
                )
        );
    }
}
