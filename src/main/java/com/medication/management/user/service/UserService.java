package com.medication.management.user.service;

import com.medication.management.user.dto.request.UserRequestDto;
import com.medication.management.user.dto.response.UserResponseDto;
import com.medication.management.user.entity.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);

    User createUser(
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String timezone
    );

    UserResponseDto register(UserRequestDto request);
}
