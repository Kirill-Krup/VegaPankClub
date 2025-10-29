package com.actisys.userservice.service.impl;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.AuthService;
import com.actisys.userservice.util.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public AuthServiceImpl(
      JwtTokenProvider jwtTokenProvider,
      UserRepository userRepository,
      UserMapper userMapper,
      PasswordEncoder passwordEncoder
  ) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDTO createUser(RegisterRequest registerRequest) {
    if (userRepository.existsByLogin(registerRequest.getLogin())) {
      throw new IllegalArgumentException("Username already taken");
    }
    if (registerRequest.getEmail() != null &&
        userRepository.existsByEmail(registerRequest.getEmail())) {
      throw new IllegalArgumentException("Email already taken");
    }

    User entity = userMapper.fromRegisterRequest(registerRequest);
    entity.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
    entity.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
    entity.setBonusCoins(0);
    entity.setWallet(0.0);

    if (entity.getRole() == 0) {
      entity.setRole(1);
    }

    User saved = userRepository.save(entity);

    String roleName = mapRole(saved.getRole());
    String token = jwtTokenProvider.generate(
        String.valueOf(saved.getId()),
        saved.getLogin(),
        List.of(roleName),
        Map.of()
    );

    UserDTO dto = userMapper.toDTO(saved);
    dto.setRole(roleName);
    dto.setToken(token);
    return dto;
  }

  @Override
  public UserDTO login(AuthRequest authRequest) {
    Optional<User> opt;
    if (authRequest.getLogin().contains("@")) {
      opt = userRepository.findByEmail(authRequest.getLogin());
    } else {
      opt = userRepository.findByLogin(authRequest.getLogin());
    }

    User user = opt.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String roleName = mapRole(user.getRole());
    String token = jwtTokenProvider.generate(
        String.valueOf(user.getId()),
        user.getLogin(),
        List.of(roleName),
        Map.of()
    );

    UserDTO dto = userMapper.toDTO(user);
    dto.setRole(roleName);
    dto.setToken(token);
    return dto;
  }

  private String mapRole(int code) {
    return code == 2 ? "ADMIN" : "USER";
  }
}
