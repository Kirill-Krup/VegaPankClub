package com.actisys.userservice.service;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.RegisterRequest;
import jakarta.validation.Valid;

public interface AuthService {

  UserDTO createUser(@Valid RegisterRequest registerRequest);

  UserDTO login(AuthRequest authRequest);
}
