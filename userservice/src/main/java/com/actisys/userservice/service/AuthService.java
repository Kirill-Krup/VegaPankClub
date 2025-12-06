package com.actisys.userservice.service;

import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.AuthResponse;
import com.actisys.userservice.dto.RegisterRequest;
import jakarta.validation.Valid;

public interface AuthService {

  /**
   * Register new user in the system.
   * Creates user with default role (USER), generates JWT token.
   * Invalidates all users cache to reflect new user in admin panel.
   *
   * @param registerRequest registration data (login, email, password, etc.)
   * @return authentication response with user data and JWT token
   * @throws IllegalArgumentException if username or email already exists
   */
  AuthResponse createUser(@Valid RegisterRequest registerRequest);


  /**
   * Authenticate user and generate JWT token.
   * Supports login by username or email.
   * Updates last login timestamp.
   *
   * @param authRequest login credentials (login/email and password)
   * @return authentication response with user data and JWT token
   * @throws IllegalArgumentException if credentials are invalid
   */
  AuthResponse login(AuthRequest authRequest);
}
