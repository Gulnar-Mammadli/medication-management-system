
package com.medication.management.auth.service;

import com.medication.management.auth.security.CustomUserDetails;
import com.medication.management.user.entity.User;
import com.medication.management.user.entity.UserRole;
import com.medication.management.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(
                "test@example.com",
                "hashed-password",
                "John",
                "Smith",
                "Europe/Tallinn"
        );
    }

    @Test
    void loadUserByUsername_shouldReturnUserRole() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        CustomUserDetails result =
                (CustomUserDetails) userDetailsService
                        .loadUserByUsername("test@example.com");

        assertThat(result.getUsername())
                .isEqualTo("test@example.com");

        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_shouldReturnAdminRole() {

        user.setRole(UserRole.ROLE_ADMIN);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        CustomUserDetails result =
                (CustomUserDetails) userDetailsService
                        .loadUserByUsername("test@example.com");

        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername(
                        "unknown@example.com"
                )
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}
