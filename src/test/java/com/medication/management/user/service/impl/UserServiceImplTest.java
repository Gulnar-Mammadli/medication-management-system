
package com.medication.management.user.service.impl;

import com.medication.management.common.exception.InvalidRequestException;
import com.medication.management.user.dto.request.ChangePasswordRequestDto;
import com.medication.management.user.dto.request.UpdateUserRequestDto;
import com.medication.management.user.dto.request.UserRequestDto;
import com.medication.management.user.dto.response.UserResponseDto;
import com.medication.management.user.entity.User;
import com.medication.management.user.entity.UserRole;
import com.medication.management.user.exception.LastAdminException;
import com.medication.management.user.exception.UserAlreadyExistsException;
import com.medication.management.user.exception.UsernameNotFoundException;
import com.medication.management.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequestDto request;

    private User createTestUser() {
        return User.create(
                "test@example.com",
                "hashed-old-password",
                "John",
                "Smith",
                "Europe/Tallinn"
        );
    }

    @BeforeEach
    void setUp() {
        request = new UserRequestDto(
                "TEST@example.com",
                "SecurePassword123!",
                "John",
                "Smith",
                "Europe/Tallinn"
        );
    }

    @Test
    void register_shouldCreateUserSuccessfully() {

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("SecurePassword123!"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto response = userService.register(request);

        // Assert
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Smith");
        assertThat(response.timezone()).isEqualTo("Europe/Tallinn");

        // Verify
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("SecurePassword123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {

        // Arrange
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with this email already exists");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_shouldStoreEncodedPassword() {
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("SecurePassword123!"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPasswordHash())
                .isEqualTo("hashed-password");

        assertThat(captor.getValue().getPasswordHash())
                .isNotEqualTo("SecurePassword123!");
    }

    @Test
    void getCurrentUser_shouldReturnUserSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserResponseDto response = userService.getCurrentUser(userId);

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Smith");
        assertThat(response.timezone()).isEqualTo("Europe/Tallinn");
    }

    @Test
    void getCurrentUser_shouldThrowExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(userId))
                .isInstanceOf(
                        com.medication.management.user.exception.UsernameNotFoundException.class
                );
    }

    @Test
    void updateCurrentUser_shouldUpdateProvidedFields() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser();

        UpdateUserRequestDto updateRequest =
                new UpdateUserRequestDto(
                        "Jonathan",
                        null,
                        null
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto response =
                userService.updateCurrentUser(userId, updateRequest);

        assertThat(response.firstName()).isEqualTo("Jonathan");
        assertThat(response.lastName()).isEqualTo("Smith");
        assertThat(response.timezone()).isEqualTo("Europe/Tallinn");
    }

    @Test
    void updateCurrentUser_shouldRejectInvalidTimezone() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser();

        UpdateUserRequestDto updateRequest =
                new UpdateUserRequestDto(
                        null,
                        null,
                        "Invalid/Timezone"
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.updateCurrentUser(userId, updateRequest)
        )
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Invalid timezone");
    }

    @Test
    void changePassword_shouldUpdatePasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser();

        ChangePasswordRequestDto request =
                new ChangePasswordRequestDto(
                        "OldPassword123!",
                        "NewPassword123!"
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "OldPassword123!",
                "hashed-old-password"
        )).thenReturn(true);

        when(passwordEncoder.matches(
                "NewPassword123!",
                "hashed-old-password"
        )).thenReturn(false);

        when(passwordEncoder.encode("NewPassword123!"))
                .thenReturn("hashed-new-password");

        userService.changePassword(userId, request);

        assertThat(user.getPasswordHash())
                .isEqualTo("hashed-new-password");

        verify(passwordEncoder).encode("NewPassword123!");
    }

    @Test
    void changePassword_shouldRejectSamePassword() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser();

        ChangePasswordRequestDto request =
                new ChangePasswordRequestDto(
                        "OldPassword123!",
                        "OldPassword123!"
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "OldPassword123!",
                "hashed-old-password"
        )).thenReturn(true);

        assertThatThrownBy(() ->
                userService.changePassword(userId, request)
        )
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage(
                        "New password must be different from current password"
                );

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_shouldAssignDefaultUserRole() {

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("SecurePassword123!"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getRole())
                .isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {

        User firstUser = User.create(
                "first@example.com", "hash1",
                "John", "Smith", "UTC"
        );

        User secondUser = User.create(
                "second@example.com", "hash2",
                "Jane", "Doe", "Europe/Tallinn"
        );

        when(userRepository.findAll())
                .thenReturn(List.of(firstUser, secondUser));

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(UserResponseDto::email)
                .containsExactly("first@example.com", "second@example.com");
    }

    @Test
    void getUserById_shouldReturnUser() {

        UUID userId = UUID.randomUUID();

        User user = User.create(
                "test@example.com", "hash",
                "John", "Smith", "UTC"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserResponseDto result = userService.getUserById(userId);

        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_shouldThrowWhenUserNotFound() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteUser_shouldDeleteUser() {

        UUID userId = UUID.randomUUID();
        User user = User.create(
                "test@example.com", "hash",
                "John", "Smith", "UTC"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowWhenUserNotFound() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteAdminWhenMultipleAdminsExist() {

        UUID userId = UUID.randomUUID();

        User user = User.create(
                "test@example.com", "hash",
                "John", "Smith", "UTC"
        );
        user.setRole(UserRole.ROLE_ADMIN);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.countByRole(UserRole.ROLE_ADMIN))
                .thenReturn(2L);

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowWhenDeletingLastAdmin() {

        UUID userId = UUID.randomUUID();
        User user = User.create(
                "test@example.com", "hash",
                "John", "Smith", "UTC"
        );
        user.setRole(UserRole.ROLE_ADMIN);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.countByRole(UserRole.ROLE_ADMIN))
                .thenReturn(1L);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(LastAdminException.class)
                .hasMessage("Cannot delete the last administrator");

        verify(userRepository, never()).delete(any(User.class));
    }
}