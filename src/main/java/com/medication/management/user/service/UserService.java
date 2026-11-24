package com.medication.management.user.service;

import com.medication.management.user.dto.request.ChangePasswordRequestDto;
import com.medication.management.user.dto.request.UpdateUserRequestDto;
import com.medication.management.user.dto.request.UserRequestDto;
import com.medication.management.user.dto.response.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDto register(UserRequestDto request);

    UserResponseDto getCurrentUser(UUID userId);

    UserResponseDto updateCurrentUser(
            UUID userId,
            UpdateUserRequestDto request
    );

    void changePassword(
            UUID userId,
            ChangePasswordRequestDto request
    );

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(UUID id);

    void deleteUser(UUID id);
}
