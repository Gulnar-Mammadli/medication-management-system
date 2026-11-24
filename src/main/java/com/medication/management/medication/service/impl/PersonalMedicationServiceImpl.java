package com.medication.management.medication.service.impl;

import com.medication.management.medication.dto.request.PersonalMedicationRequestDto;
import com.medication.management.medication.dto.response.PersonalMedicationResponseDto;
import com.medication.management.medication.entity.PersonalMedication;
import com.medication.management.medication.enums.MedicationStatus;
import com.medication.management.medication.exception.MedicationNotFoundException;
import com.medication.management.medication.repository.PersonalMedicationRepository;
import com.medication.management.medication.service.PersonalMedicationService;
import com.medication.management.user.entity.User;
import com.medication.management.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalMedicationServiceImpl implements PersonalMedicationService {

    private final PersonalMedicationRepository medicationRepository;
    private final UserRepository userRepository;

    @Override
    public PersonalMedicationResponseDto createMedication(
            UUID userId,
            PersonalMedicationRequestDto request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        PersonalMedication medication = new PersonalMedication();

        medication.setUser(user);
        medication.setName(request.name().trim());
        medication.setDosage(request.dosage().trim());
        medication.setInstructions(request.instructions());
        medication.setStartDate(request.startDate());
        medication.setEndDate(request.endDate());

        PersonalMedication saved =
                medicationRepository.save(medication);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonalMedicationResponseDto> getAllMedications(
            UUID userId,
            MedicationStatus status,
            Pageable pageable
    ) {
        Page<PersonalMedication> medications;

        if (status == null) {
            medications = medicationRepository.findAllByUserId(
                    userId,
                    pageable
            );
        } else {
            medications = medicationRepository.findAllByUserIdAndStatus(
                    userId,
                    status,
                    pageable
            );
        }

        return medications.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalMedicationResponseDto getMedicationById(
            UUID userId,
            UUID id
    ) {
        PersonalMedication medication =
                findMedicationByIdAndUserId(id, userId);

        return mapToResponse(medication);
    }

    @Override
    public PersonalMedicationResponseDto updateMedication(
            UUID userId,
            UUID id,
            PersonalMedicationRequestDto request
    ) {
        PersonalMedication medication =
                findMedicationByIdAndUserId(id, userId);

        medication.setName(request.name().trim());
        medication.setDosage(request.dosage().trim());
        medication.setInstructions(request.instructions());
        medication.setStartDate(request.startDate());
        medication.setEndDate(request.endDate());

        return mapToResponse(
                medicationRepository.save(medication)
        );
    }

    @Override
    public void deleteMedication(UUID userId, UUID id) {
        PersonalMedication medication =
                findMedicationByIdAndUserId(id, userId);

        medicationRepository.delete(medication);
    }

    @Override
    public PersonalMedicationResponseDto updateMedicationStatus(
            UUID userId,
            UUID id,
            MedicationStatus status
    ) {
        PersonalMedication medication =
                findMedicationByIdAndUserId(id, userId);

        medication.setStatus(status);

        return mapToResponse(
                medicationRepository.save(medication)
        );
    }

    private PersonalMedication findMedicationByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        return medicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new MedicationNotFoundException(
                                "Medication not found"
                        )
                );
    }

    private PersonalMedicationResponseDto mapToResponse(
            PersonalMedication medication
    ) {
        return new PersonalMedicationResponseDto(
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                medication.getInstructions(),
                medication.getStartDate(),
                medication.getEndDate(),
                medication.getStatus(),
                medication.getCreatedAt(),
                medication.getUpdatedAt()
        );
    }
}
