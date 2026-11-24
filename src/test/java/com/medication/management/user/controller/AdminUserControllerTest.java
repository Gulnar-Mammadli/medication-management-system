
package com.medication.management.user.controller;

import com.medication.management.auth.jwt.JwtService;
import com.medication.management.auth.service.CustomUserDetailsService;
import com.medication.management.auth.service.TokenRevocationService;
import com.medication.management.common.exception.GlobalExceptionHandler;
import com.medication.management.config.SecurityConfig;
import com.medication.management.user.dto.response.UserResponseDto;
import com.medication.management.user.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private TokenRevocationService tokenRevocationService;

    private UserResponseDto createUserResponse() {
        return new UserResponseDto(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Smith",
                "Europe/Tallinn",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturn200ForAdmin() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of(createUserResponse()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email")
                        .value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturn200ForAdmin() throws Exception {

        UUID id = UUID.randomUUID();

        when(userService.getUserById(id))
                .thenReturn(createUserResponse());

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturn204ForAdmin() throws Exception {

        UUID id = UUID.randomUUID();

        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturn403ForRegularUser() throws Exception {

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void getAllUsers_shouldRejectUnauthenticatedUser() throws Exception {

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}
