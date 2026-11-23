package com.medication.management.user.controller;

import com.medication.management.common.exception.GlobalExceptionHandler;
import com.medication.management.config.SecurityConfig;
import com.medication.management.user.dto.request.UserRequestDto;
import com.medication.management.user.dto.response.UserResponseDto;
import com.medication.management.user.exception.UserAlreadyExistsException;
import com.medication.management.user.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void register_shouldReturn201WhenSuccessful() throws Exception {

        UserRequestDto request = new UserRequestDto(
                "test@example.com",
                "SecurePassword123!",
                "John",
                "Smith",
                "Europe/Tallinn"
        );

        UserResponseDto response = new UserResponseDto(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Smith",
                "Europe/Tallinn",
                Instant.now()
        );

        when(userService.register(any(UserRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_shouldReturn400WhenEmailIsInvalid() throws Exception {

        UserRequestDto request = new UserRequestDto(
                "invalid-email",
                "SecurePassword123!",
                "John",
                "Smith",
                "Europe/Tallinn"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenPasswordIsMissing() throws Exception {

        UserRequestDto request = new UserRequestDto(
                "test@example.com",
                "",
                "John",
                "Smith",
                "Europe/Tallinn"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409WhenEmailAlreadyExists() throws Exception {

        UserRequestDto request = new UserRequestDto(
                "test@example.com",
                "SecurePassword123!",
                "John",
                "Smith",
                "Europe/Tallinn"
        );

        when(userService.register(any(UserRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException(
                        "User with this email already exists"
                ));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
