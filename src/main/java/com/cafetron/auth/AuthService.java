package com.cafetron.auth;

import com.cafetron.auth.dto.AuthResponse;
import com.cafetron.auth.dto.LoginRequest;
import com.cafetron.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}