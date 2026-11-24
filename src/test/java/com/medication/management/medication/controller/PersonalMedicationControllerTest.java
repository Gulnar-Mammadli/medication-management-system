package com.medication.management.medication.controller;

import com.medication.management.auth.security.CustomUserDetails;
import com.medication.management.auth.jwt.JwtService;
import com.medication.management.auth.service.CustomUserDetailsService;
import com.medication.management.auth.service.TokenRevocationService;
import com.medication.management.common.exception.GlobalExceptionHandler;
import com.medication.management.config.SecurityConfig;
import com.medication.management.medication.dto.request.PersonalMedicationRequestDto;
import com.medication.management.medication.dto.response.PersonalMedicationResponseDto;
import com.medication.management.medication.enums.MedicationStatus;
import com.medication.management.medication.exception.MedicationNotFoundException;
import com.medication.management.medication.service.PersonalMedicationService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonalMedicationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PersonalMedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonalMedicationService medicationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private TokenRevocationService tokenRevocationService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID medicationId;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        medicationId = UUID.randomUUID();

        userDetails = new CustomUserDetails(
                userId,
                "test@example.com",
                "hashed-password",
                List.of()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void createMedication_shouldReturn201WhenSuccessful() throws Exception {

        PersonalMedicationRequestDto request =
                new PersonalMedicationRequestDto(
                        "Ibuprofen",
                        "200 mg",
                        "Take with food",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30)
                );

        PersonalMedicationResponseDto response =
                new PersonalMedicationResponseDto(
                        medicationId,
                        "Ibuprofen",
                        "200 mg",
                        "Take with food",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30),
                        MedicationStatus.ACTIVE,
                        Instant.now(),
                        Instant.now()
                );

        when(medicationService.createMedication(
                eq(userId),
                any(PersonalMedicationRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/v1/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(medicationId.toString()))
                .andExpect(jsonPath("$.name").value("Ibuprofen"))
                .andExpect(jsonPath("$.dosage").value("200 mg"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(medicationService).createMedication(
                eq(userId),
                any(PersonalMedicationRequestDto.class)
        );
    }


    @Test
    void getAllMedications_shouldReturn200() throws Exception {

        PersonalMedicationResponseDto medication =
                new PersonalMedicationResponseDto(
                        medicationId,
                        "Ibuprofen",
                        "200 mg",
                        "Take with food",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30),
                        MedicationStatus.ACTIVE,
                        Instant.now(),
                        Instant.now()
                );

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("createdAt").descending()
        );

        Page<PersonalMedicationResponseDto> medicationPage =
                new PageImpl<>(
                        List.of(medication),
                        pageable,
                        1
                );

        when(medicationService.getAllMedications(
                eq(userId),
                isNull(),
                eq(pageable)
        )).thenReturn(medicationPage);

        mockMvc.perform(get("/api/v1/medications")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(medicationId.toString()))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Ibuprofen"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(medicationService).getAllMedications(
                eq(userId),
                isNull(),
                eq(pageable)
        );
    }


    @Test
    void getAllMedications_shouldFilterByStatus() throws Exception {

        PersonalMedicationResponseDto medication =
                new PersonalMedicationResponseDto(
                        medicationId,
                        "Ibuprofen",
                        "200 mg",
                        "Take with food",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30),
                        MedicationStatus.ACTIVE,
                        Instant.now(),
                        Instant.now()
                );

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("createdAt").descending()
        );

        Page<PersonalMedicationResponseDto> medicationPage =
                new PageImpl<>(List.of(medication), pageable, 1);

        when(medicationService.getAllMedications(
                eq(userId),
                eq(MedicationStatus.ACTIVE),
                eq(pageable)
        )).thenReturn(medicationPage);

        mockMvc.perform(get("/api/v1/medications")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status")
                        .value("ACTIVE"));

        verify(medicationService).getAllMedications(
                eq(userId),
                eq(MedicationStatus.ACTIVE),
                eq(pageable)
        );
    }


    @Test
    void getAllMedications_shouldReturn400WhenStatusIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/medications")
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(medicationService);
    }


    @Test
    void getMedicationById_shouldReturn200() throws Exception {

        PersonalMedicationResponseDto medication =
                new PersonalMedicationResponseDto(
                        medicationId,
                        "Ibuprofen",
                        "200 mg",
                        "Take with food",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30),
                        MedicationStatus.ACTIVE,
                        Instant.now(),
                        Instant.now()
                );

        when(medicationService.getMedicationById(userId, medicationId))
                .thenReturn(medication);

        mockMvc.perform(get("/api/v1/medications/{id}", medicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(medicationId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Ibuprofen"))
                .andExpect(jsonPath("$.dosage")
                        .value("200 mg"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));

        verify(medicationService)
                .getMedicationById(userId, medicationId);
    }


    @Test
    void updateMedication_shouldReturn200() throws Exception {

        PersonalMedicationRequestDto request =
                new PersonalMedicationRequestDto(
                        "Ibuprofen",
                        "400 mg",
                        "Take after meals",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30)
                );

        PersonalMedicationResponseDto response =
                new PersonalMedicationResponseDto(
                        medicationId,
                        "Ibuprofen",
                        "400 mg",
                        "Take after meals",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30),
                        MedicationStatus.ACTIVE,
                        Instant.now(),
                        Instant.now()
                );

        when(medicationService.updateMedication(
                eq(userId),
                eq(medicationId),
                any(PersonalMedicationRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/medications/{id}", medicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(medicationId.toString()))
                .andExpect(jsonPath("$.dosage")
                        .value("400 mg"))
                .andExpect(jsonPath("$.instructions")
                        .value("Take after meals"));

        verify(medicationService).updateMedication(
                eq(userId),
                eq(medicationId),
                any(PersonalMedicationRequestDto.class)
        );
    }


    @Test
    void deleteMedication_shouldReturn204() throws Exception {

        doNothing().when(medicationService)
                .deleteMedication(userId, medicationId);

        mockMvc.perform(delete(
                        "/api/v1/medications/{id}",
                        medicationId
                ))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(medicationService)
                .deleteMedication(userId, medicationId);
    }


    @Test
    void updateMedicationStatus_shouldReturn200() throws Exception {

        PersonalMedicationResponseDto response =
                new PersonalMedicationResponseDto(
                        medicationId,
                        "Ibuprofen",
                        "200 mg",
                        "Take with food",
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 30),
                        MedicationStatus.COMPLETED,
                        Instant.now(),
                        Instant.now()
                );

        when(medicationService.updateMedicationStatus(
                userId,
                medicationId,
                MedicationStatus.COMPLETED
        )).thenReturn(response);

        mockMvc.perform(patch(
                        "/api/v1/medications/{id}/status",
                        medicationId
                )
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(medicationId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("COMPLETED"));

        verify(medicationService).updateMedicationStatus(
                userId,
                medicationId,
                MedicationStatus.COMPLETED
        );
    }


    @Test
    void getAllMedications_shouldRejectUnauthenticatedUser() throws Exception {

        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/medications"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(medicationService);
    }


    @Test
    void getMedicationById_shouldReturn404WhenMedicationBelongsToAnotherUser()
            throws Exception {

        when(medicationService.getMedicationById(userId, medicationId))
                .thenThrow(new MedicationNotFoundException(
                        "Medication not found"
                ));

        mockMvc.perform(get(
                        "/api/v1/medications/{id}",
                        medicationId
                ))
                .andExpect(status().isNotFound());

        verify(medicationService)
                .getMedicationById(userId, medicationId);
    }

}
