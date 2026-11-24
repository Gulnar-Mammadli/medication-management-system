package com.medication.management.user.service.impl;

import com.medication.management.user.dto.request.UserRequestDto;
import com.medication.management.user.dto.response.UserResponseDto;
import com.medication.management.user.entity.User;
import com.medication.management.user.exception.UserAlreadyExistsException;
import com.medication.management.user.exception.UsernameNotFoundException;
import com.medication.management.user.repository.UserRepository;
import com.medication.management.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                savedUser.getCreatedAt()
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
                user.getCreatedAt()
        );
    }
}
