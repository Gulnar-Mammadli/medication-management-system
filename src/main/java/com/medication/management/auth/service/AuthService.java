package com.medication.management.auth.service;

import com.medication.management.auth.dto.request.LoginRequestDto;
import com.medication.management.auth.dto.response.LoginResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto request);
}
