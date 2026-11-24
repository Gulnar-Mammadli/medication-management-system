package com.medication.management.auth.service.impl;

import com.medication.management.auth.dto.request.LoginRequestDto;
import com.medication.management.auth.dto.response.LoginResponseDto;
import com.medication.management.auth.jwt.JwtService;
import com.medication.management.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public LoginResponseDto login(LoginRequestDto request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                normalizedEmail,
                                request.password()
                        )
                );

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails userDetails)) {
            throw new IllegalStateException(
                    "Authentication principal is not a UserDetails instance"
            );
        }

        String token = jwtService.generateToken(userDetails);

        return new LoginResponseDto(
                token,
                "Bearer",
                expiration
        );
    }
}