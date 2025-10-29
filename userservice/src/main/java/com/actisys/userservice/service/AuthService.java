package com.actisys.userservice.service;

import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.AuthResponse;
import com.actisys.userservice.dto.RegisterRequest;
import jakarta.validation.Valid;

public interface AuthService {

  AuthResponse createUser(@Valid RegisterRequest registerRequest);

  AuthResponse login(AuthRequest authRequest);
}
