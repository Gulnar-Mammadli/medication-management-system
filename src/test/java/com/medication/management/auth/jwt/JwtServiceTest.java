
package com.medication.management.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private UserDetails userDetails;

    private static final String SECRET = Base64.getEncoder()
            .encodeToString(
                    "this-is-a-test-secret-key-at-least-32-bytes"
                            .getBytes()
            );

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                SECRET
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                3600000L
        );

        userDetails = User.withUsername("test@example.com")
                .password("hashed-password")
                .authorities(List.of())
                .build();
    }

    @Test
    void generateToken_shouldGenerateValidToken() {

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {

        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(
                token,
                userDetails
        );

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {

        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = User.withUsername("another@example.com")
                .password("hashed-password")
                .authorities(List.of())
                .build();

        boolean valid = jwtService.isTokenValid(
                token,
                anotherUser
        );

        assertThat(valid).isFalse();
    }

    @Test
    void extractUsername_shouldThrowExceptionForInvalidToken() {

        assertThatThrownBy(() ->
                jwtService.extractUsername("invalid-token")
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void isTokenValid_shouldThrowExceptionForExpiredToken() {

        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                -1000L
        );

        String expiredToken = jwtService.generateToken(userDetails);

        assertThatThrownBy(() ->
                jwtService.isTokenValid(expiredToken, userDetails)
        ).isInstanceOf(RuntimeException.class);
    }
}
