package com.actisys.userservice.service.impl;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.AuthResponse;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.AuthService;
import com.actisys.userservice.util.JwtTokenProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  /**
   * Register new user in the system.
   * Creates user with default role (USER), generates JWT token.
   * Invalidates all users cache to reflect new user in admin panel.
   *
   * @param registerRequest registration data (login, email, password, etc.)
   * @return authentication response with user data and JWT token
   * @throws IllegalArgumentException if username or email already exists
   */
  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
  public AuthResponse createUser(RegisterRequest registerRequest) {
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

    return new AuthResponse(dto, token);
  }

  /**
   * Authenticate user and generate JWT token.
   * Supports login by username or email.
   * Updates last login timestamp.
   *
   * @param authRequest login credentials (login/email and password)
   * @return authentication response with user data and JWT token
   * @throws IllegalArgumentException if credentials are invalid
   */
  @Override
  @Transactional
  public AuthResponse login(AuthRequest authRequest) {
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

    user.setLastLogin(new Timestamp(System.currentTimeMillis()));
    userRepository.save(user);

    String roleName = mapRole(user.getRole());
    String token = jwtTokenProvider.generate(
        String.valueOf(user.getId()),
        user.getLogin(),
        List.of(roleName),
        Map.of()
    );

    UserDTO dto = userMapper.toDTO(user);
    dto.setRole(roleName);

    return new AuthResponse(dto, token);
  }

  /**
   * Map role code to role name.
   *
   * @param code role code (1 = USER, 2 = ADMIN)
   * @return role name as string
   */
  private String mapRole(int code) {
    return code == 2 ? "ADMIN" : "USER";
  }
}
