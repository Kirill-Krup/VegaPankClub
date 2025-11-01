package com.actisys.userservice.controller;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.AuthResponse;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  @Value("${jwt.ttl-seconds:860000}")
  private long ttlSeconds;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(
      @RequestBody RegisterRequest request,
      HttpServletResponse response
  ) {
    AuthResponse authResponse = authService.createUser(request);

    ResponseCookie cookie = ResponseCookie.from("auth_token", authResponse.getToken())
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(ttlSeconds)
        .sameSite("Lax")
        .build();

    response.addHeader("Set-Cookie", cookie.toString());

    return ResponseEntity.ok(authResponse.getUser());
  }

  @PostMapping("/login")
  public ResponseEntity<UserDTO> login(
      @RequestBody AuthRequest request,
      HttpServletResponse response
  ) {
    AuthResponse authResponse = authService.login(request);

    ResponseCookie cookie = ResponseCookie.from("auth_token", authResponse.getToken())
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(ttlSeconds)
        .sameSite("Lax")
        .build();

    response.addHeader("Set-Cookie", cookie.toString());

    return ResponseEntity.ok(authResponse.getUser());
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("auth_token", "")
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
    return ResponseEntity.ok("Logged out successfully");
  }
}