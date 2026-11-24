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
import com.medication.management.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponseDto register(UserRequestDto request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException(
                    "User with this email already exists"
            );
        }

        String passwordHash = passwordEncoder.encode(
                request.password()
        );

        User user = User.create(
                normalizedEmail,
                passwordHash,
                request.firstName().trim(),
                request.lastName().trim(),
                request.timezone()
        );

        User savedUser = userRepository.save(user);

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getTimezone(),
                savedUser.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Override
    public UserResponseDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getTimezone(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public UserResponseDto updateCurrentUser(
            UUID userId,
            UpdateUserRequestDto request
    ) {
        // Ensure at least one field was provided
        if (request.firstName() == null
                && request.lastName() == null
                && request.timezone() == null) {
            throw new InvalidRequestException(
                    "At least one field must be provided"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }

        if (request.timezone() != null) {
            String timezone = request.timezone().trim();

            if (!ZoneId.getAvailableZoneIds().contains(timezone)) {
                throw new InvalidRequestException("Invalid timezone");
            }

            user.setTimezone(timezone);
        }

        User updatedUser = userRepository.save(user);

        return new UserResponseDto(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getTimezone(),
                updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void changePassword(
            UUID userId,
            ChangePasswordRequestDto request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new InvalidRequestException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPasswordHash()
        )) {
            throw new InvalidRequestException(
                    "New password must be different from current password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );
        userRepository.save(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        return toResponseDto(user);
    }

    private UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getTimezone(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
    @Override
    @Transactional
    public void deleteUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        if (user.getRole() == UserRole.ROLE_ADMIN) {

            long adminCount = userRepository.countByRole(
                    UserRole.ROLE_ADMIN
            );

            if (adminCount <= 1) {
                throw new LastAdminException(
                        "Cannot delete the last administrator"
                );
            }
        }

        userRepository.delete(user);
    }
}
