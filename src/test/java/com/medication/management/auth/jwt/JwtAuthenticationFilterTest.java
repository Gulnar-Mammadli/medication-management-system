
package com.medication.management.auth.jwt;

import com.medication.management.auth.security.CustomUserDetails;
import com.medication.management.auth.service.CustomUserDetailsService;
import com.medication.management.auth.service.TokenRevocationService;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userDetails = new CustomUserDetails(
                UUID.randomUUID(),
                "test@example.com",
                "hashed-password",
                List.of()
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldAuthenticateWhenTokenIsValid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer valid-token");

        when(tokenRevocationService.isRevoked("valid-token"))
                .thenReturn(false);

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("test@example.com");

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("valid-token", userDetails))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);

        verify(tokenRevocationService).isRevoked("valid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(
                jwtService,
                userDetailsService,
                tokenRevocationService
        );
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWhenTokenIsInvalid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer invalid-token");

        when(tokenRevocationService.isRevoked("invalid-token"))
                .thenReturn(false);

        when(jwtService.extractUsername("invalid-token"))
                .thenReturn("test@example.com");

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("invalid-token", userDetails))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWhenTokenIsRevoked()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer revoked-token");

        when(tokenRevocationService.isRevoked("revoked-token"))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(tokenRevocationService).isRevoked("revoked-token");

        verifyNoInteractions(jwtService, userDetailsService);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextWhenTokenThrowsJwtException()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer expired-token");

        when(tokenRevocationService.isRevoked("expired-token"))
                .thenReturn(false);

        when(jwtService.extractUsername("expired-token"))
                .thenThrow(new JwtException("Token expired"));

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextWhenTokenIsMalformed()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Bearer malformed-token");

        when(tokenRevocationService.isRevoked("malformed-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }
}