package com.medication.management.auth.controller;

import com.medication.management.auth.dto.request.LoginRequestDto;
import com.medication.management.auth.dto.response.LoginResponseDto;
import com.medication.management.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorization
    ) {

        String token = authorization.substring(7);

        authService.logout(token);

        return ResponseEntity.noContent().build();
    }
}
