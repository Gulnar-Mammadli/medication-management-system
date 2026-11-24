
package com.medication.management.auth.service.impl;

import com.medication.management.auth.dto.request.LoginRequestDto;
import com.medication.management.auth.dto.response.LoginResponseDto;
import com.medication.management.auth.jwt.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {

        LoginRequestDto request = new LoginRequestDto(
                "test@example.com",
                "SecurePassword123!"
        );

        UserDetails userDetails = User.withUsername("test@example.com")
                .password("hashed-password")
                .authorities(List.of())
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn("mock-jwt-token");

        LoginResponseDto response = authService.login(request);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ));

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_shouldNormalizeEmailBeforeAuthentication() {

        LoginRequestDto request = new LoginRequestDto(
                " TEST@EXAMPLE.COM ",
                "SecurePassword123!"
        );

        UserDetails userDetails = User.withUsername("test@example.com")
                .password("hashed-password")
                .authorities(List.of())
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn("mock-jwt-token");

        authService.login(request);

        verify(authenticationManager).authenticate(argThat(token ->
                token.getPrincipal().equals("test@example.com")
                        && token.getCredentials().equals("SecurePassword123!")
        ));
    }

    @Test
    void login_shouldThrowExceptionWhenCredentialsAreInvalid() {

        LoginRequestDto request = new LoginRequestDto(
                "test@example.com",
                "WrongPassword123!"
        );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }
}
