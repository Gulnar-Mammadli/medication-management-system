package com.medication.management.medication.service;

import com.medication.management.medication.dto.request.PersonalMedicationRequestDto;
import com.medication.management.medication.dto.response.PersonalMedicationResponseDto;
import com.medication.management.medication.enums.MedicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PersonalMedicationService {

    PersonalMedicationResponseDto createMedication(
            UUID userId,
            PersonalMedicationRequestDto request
    );

    Page<PersonalMedicationResponseDto> getAllMedications(
            UUID userId,
            MedicationStatus status,
            Pageable pageable
    );

    PersonalMedicationResponseDto getMedicationById(
            UUID userId,
            UUID id
    );

    PersonalMedicationResponseDto updateMedication(
            UUID userId,
            UUID id,
            PersonalMedicationRequestDto request
    );

    void deleteMedication(UUID userId, UUID id);

    PersonalMedicationResponseDto updateMedicationStatus(
            UUID userId,
            UUID id,
            MedicationStatus status
    );
}