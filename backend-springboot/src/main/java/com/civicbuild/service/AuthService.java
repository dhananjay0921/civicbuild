package com.civicbuild.service;

import com.civicbuild.dto.request.LoginRequest;
import com.civicbuild.dto.request.RegisterRequest;
import com.civicbuild.dto.response.AuthResponse;
import com.civicbuild.entity.User;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    User getCurrentUser();
}
