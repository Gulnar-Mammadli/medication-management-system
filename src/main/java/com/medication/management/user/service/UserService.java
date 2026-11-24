package com.medication.management.user.service;

import com.medication.management.user.dto.request.UserRequestDto;
import com.medication.management.user.dto.response.UserResponseDto;
import java.util.UUID;

public interface UserService {

    UserResponseDto register(UserRequestDto request);

    UserResponseDto getCurrentUser(UUID userId);
}
