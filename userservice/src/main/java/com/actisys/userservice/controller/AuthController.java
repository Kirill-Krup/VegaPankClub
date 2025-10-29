package com.actisys.userservice.controller;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(final AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<UserDTO> createUser(@Valid @RequestBody RegisterRequest registerRequest) {
    UserDTO newUser = authService.createUser(registerRequest);
    return ResponseEntity.ok().body(newUser);
  }

  @PostMapping("/login")
  public ResponseEntity<UserDTO> login(@RequestBody AuthRequest authRequest) {
    UserDTO dto = authService.login(authRequest);
    return ResponseEntity.ok().body(dto);
  }

}
