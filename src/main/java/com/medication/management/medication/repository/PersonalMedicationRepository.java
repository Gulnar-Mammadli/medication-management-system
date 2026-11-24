package com.medication.management.medication.repository;

import com.medication.management.medication.entity.PersonalMedication;
import com.medication.management.medication.enums.MedicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PersonalMedicationRepository
        extends JpaRepository<PersonalMedication, UUID> {

    Page<PersonalMedication> findAllByUserId(UUID userId, Pageable pageable);

    Page<PersonalMedication> findAllByUserIdAndStatus(
            UUID userId,
            MedicationStatus status,
            Pageable pageable
    );

    Optional<PersonalMedication> findByIdAndUserId(
            UUID id,
            UUID userId
    );
}