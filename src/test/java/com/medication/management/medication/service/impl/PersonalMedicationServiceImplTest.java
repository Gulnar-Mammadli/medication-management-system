package com.medication.management.medication.service.impl;

import com.medication.management.medication.dto.request.PersonalMedicationRequestDto;
import com.medication.management.medication.dto.response.PersonalMedicationResponseDto;
import com.medication.management.medication.entity.PersonalMedication;
import com.medication.management.medication.enums.MedicationStatus;
import com.medication.management.medication.exception.MedicationNotFoundException;
import com.medication.management.medication.repository.PersonalMedicationRepository;
import com.medication.management.user.entity.User;
import com.medication.management.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalMedicationServiceImplTest {

    @Mock
    private PersonalMedicationRepository medicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PersonalMedicationServiceImpl medicationService;

    private UUID userId;
    private UUID medicationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        medicationId = UUID.randomUUID();
    }

    @Test
    void createMedication_shouldReturnSavedMedication() {

        User user = User.create(
                "test@example.com", "hash",
                "John", "Smith", "UTC"
        );
        user.setId(userId);

        PersonalMedicationRequestDto request =
                new PersonalMedicationRequestDto(
                        "Ibuprofen",
                        "200 mg",
                        "Take with food",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30)
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(medicationRepository.save(any(PersonalMedication.class)))
                .thenAnswer(invocation -> {
                    PersonalMedication medication =
                            invocation.getArgument(0);

                    medication.setId(medicationId);
                    medication.setStatus(MedicationStatus.ACTIVE);
                    medication.setCreatedAt(Instant.now());
                    medication.setUpdatedAt(Instant.now());

                    return medication;
                });

        PersonalMedicationResponseDto response =
                medicationService.createMedication(userId, request);

        assertThat(response.id()).isEqualTo(medicationId);
        assertThat(response.name()).isEqualTo("Ibuprofen");
        assertThat(response.dosage()).isEqualTo("200 mg");
        assertThat(response.status()).isEqualTo(MedicationStatus.ACTIVE);

        verify(userRepository).findById(userId);
        verify(medicationRepository).save(any(PersonalMedication.class));
    }


    @Test
    void getAllMedications_shouldReturnCurrentUsersMedications() {

        PersonalMedication medication = createMedication();

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("createdAt").descending()
        );

        Page<PersonalMedication> medicationPage =
                new PageImpl<>(
                        List.of(medication),
                        pageable,
                        1
                );

        when(medicationRepository.findAllByUserId(userId, pageable))
                .thenReturn(medicationPage);

        Page<PersonalMedicationResponseDto> response =
                medicationService.getAllMedications(userId, null, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).id())
                .isEqualTo(medicationId);
        assertThat(response.getContent().get(0).name())
                .isEqualTo("Ibuprofen");

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getNumber()).isZero();

        verify(medicationRepository)
                .findAllByUserId(userId, pageable);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllMedications_shouldFilterByStatus() {

        PersonalMedication medication = createMedication();
        medication.setStatus(MedicationStatus.ACTIVE);

        Pageable pageable = PageRequest.of(0, 10);

        Page<PersonalMedication> medicationPage =
                new PageImpl<>(List.of(medication), pageable, 1);

        when(medicationRepository.findAllByUserIdAndStatus(
                userId,
                MedicationStatus.ACTIVE,
                pageable
        )).thenReturn(medicationPage);

        Page<PersonalMedicationResponseDto> response =
                medicationService.getAllMedications(
                        userId,
                        MedicationStatus.ACTIVE,
                        pageable
                );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).status())
                .isEqualTo(MedicationStatus.ACTIVE);

        verify(medicationRepository).findAllByUserIdAndStatus(
                userId,
                MedicationStatus.ACTIVE,
                pageable
        );

        verify(medicationRepository, never())
                .findAllByUserId(any(UUID.class), any(Pageable.class));
    }

    @Test
    void getMedicationById_shouldReturnMedication() {

        PersonalMedication medication = createMedication();

        when(medicationRepository.findByIdAndUserId(medicationId, userId))
                .thenReturn(Optional.of(medication));

        PersonalMedicationResponseDto response =
                medicationService.getMedicationById(userId, medicationId);

        assertThat(response.id()).isEqualTo(medicationId);
        assertThat(response.name()).isEqualTo("Ibuprofen");
        assertThat(response.dosage()).isEqualTo("200 mg");

        verify(medicationRepository)
                .findByIdAndUserId(medicationId, userId);
    }

    @Test
    void getMedicationById_shouldThrowWhenMedicationNotFound() {

        when(medicationRepository.findByIdAndUserId(medicationId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                medicationService.getMedicationById(userId, medicationId)
        )
                .isInstanceOf(MedicationNotFoundException.class)
                .hasMessage("Medication not found");

        verify(medicationRepository)
                .findByIdAndUserId(medicationId, userId);
    }

    @Test
    void updateMedication_shouldUpdateMedication() {

        PersonalMedication medication = createMedication();

        PersonalMedicationRequestDto request =
                new PersonalMedicationRequestDto(
                        "Ibuprofen",
                        "400 mg",
                        "Take after meals",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30)
                );

        when(medicationRepository.findByIdAndUserId(medicationId, userId))
                .thenReturn(Optional.of(medication));

        when(medicationRepository.save(medication))
                .thenReturn(medication);

        PersonalMedicationResponseDto response =
                medicationService.updateMedication(
                        userId,
                        medicationId,
                        request
                );

        assertThat(response.id()).isEqualTo(medicationId);
        assertThat(response.dosage()).isEqualTo("400 mg");
        assertThat(response.instructions()).isEqualTo("Take after meals");
        assertThat(response.status()).isEqualTo(MedicationStatus.ACTIVE);

        verify(medicationRepository)
                .findByIdAndUserId(medicationId, userId);

        verify(medicationRepository).save(medication);
    }

    @Test
    void deleteMedication_shouldDeleteMedication() {

        PersonalMedication medication = createMedication();

        when(medicationRepository.findByIdAndUserId(medicationId, userId))
                .thenReturn(Optional.of(medication));

        medicationService.deleteMedication(userId, medicationId);

        verify(medicationRepository)
                .findByIdAndUserId(medicationId, userId);

        verify(medicationRepository).delete(medication);
    }

    @Test
    void deleteMedication_shouldThrowWhenMedicationNotFound() {

        when(medicationRepository.findByIdAndUserId(medicationId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                medicationService.deleteMedication(userId, medicationId)
        )
                .isInstanceOf(MedicationNotFoundException.class)
                .hasMessage("Medication not found");

        verify(medicationRepository, never())
                .delete(any(PersonalMedication.class));
    }

    @Test
    void updateMedicationStatus_shouldUpdateStatus() {

        PersonalMedication medication = createMedication();

        when(medicationRepository.findByIdAndUserId(medicationId, userId))
                .thenReturn(Optional.of(medication));

        when(medicationRepository.save(medication))
                .thenReturn(medication);

        PersonalMedicationResponseDto response =
                medicationService.updateMedicationStatus(
                        userId,
                        medicationId,
                        MedicationStatus.COMPLETED
                );

        assertThat(response.id()).isEqualTo(medicationId);
        assertThat(response.status()).isEqualTo(MedicationStatus.COMPLETED);

        verify(medicationRepository)
                .findByIdAndUserId(medicationId, userId);

        verify(medicationRepository).save(medication);
    }

    @Test
    void updateMedicationStatus_shouldThrowWhenMedicationNotFound() {

        when(medicationRepository.findByIdAndUserId(medicationId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                medicationService.updateMedicationStatus(
                        userId,
                        medicationId,
                        MedicationStatus.COMPLETED
                )
        )
                .isInstanceOf(MedicationNotFoundException.class)
                .hasMessage("Medication not found");

        verify(medicationRepository, never())
                .save(any(PersonalMedication.class));
    }

    private PersonalMedication createMedication() {

        PersonalMedication medication = new PersonalMedication();

        medication.setId(medicationId);
        medication.setName("Ibuprofen");
        medication.setDosage("200 mg");
        medication.setInstructions("Take with food");
        medication.setStartDate(LocalDate.of(2026, 11, 24));
        medication.setStatus(MedicationStatus.ACTIVE);
        medication.setCreatedAt(Instant.now());
        medication.setUpdatedAt(Instant.now());

        return medication;
    }
}
