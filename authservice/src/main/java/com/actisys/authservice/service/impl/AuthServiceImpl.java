package com.actisys.authservice.service.impl;

import com.actisys.authservice.client.UserServiceClient;
import com.actisys.authservice.dto.AuthRequest;
import com.actisys.authservice.dto.AuthResponse;
import com.actisys.authservice.dto.RegNewUserDTO;
import com.actisys.authservice.dto.RegisterRequest;
import com.actisys.authservice.exception.UserNotFoundException;
import com.actisys.authservice.model.Credential;
import com.actisys.authservice.repository.AuthRepository;
import com.actisys.authservice.service.AuthService;
import com.actisys.authservice.util.JwtTokenProvider;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthRepository authRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserServiceClient userServiceClient;

  public AuthServiceImpl(
      JwtTokenProvider jwtTokenProvider,
      AuthRepository authRepository,
      PasswordEncoder passwordEncoder,
      UserServiceClient userServiceClient
  ) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.authRepository = authRepository;
    this.passwordEncoder = passwordEncoder;
    this.userServiceClient = userServiceClient;
  }


  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
  public AuthResponse createUser(RegisterRequest registerRequest) {
    if (authRepository.existsByLogin(registerRequest.getLogin())) {
      throw new IllegalArgumentException("Username already taken");
    }
    Credential credentials = Credential.builder()
            .login(registerRequest.getLogin())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .build();
    if(registerRequest.getRememberMe() != null && registerRequest.getRememberMe()) {
      credentials.setRefreshToken(jwtTokenProvider.generateRefreshToken(registerRequest.getLogin()));
    }

    RegNewUserDTO userForSave = RegNewUserDTO.builder()
            .login(registerRequest.getLogin())
            .email(registerRequest.getEmail())
            .phone(registerRequest.getPhone())
            .fullName(registerRequest.getFullName())
            .birthDate(registerRequest.getBirthDate())
            .build();

    Long userId = userServiceClient.regNewUser(userForSave);
    if(userId != 0){
      credentials.setUserId(userId);
      authRepository.save(credentials);
      return new AuthResponse(jwtTokenProvider.generate(String.valueOf(userId),
              credentials.getLogin(),
              List.of("USER"),
              Map.of()), credentials.getRefreshToken());
    }
    throw new IllegalArgumentException("Error with registration");
  }


  @Override
  @Transactional
  public AuthResponse login(AuthRequest authRequest) {
    Credential credentials = authRepository.findByLogin((authRequest.getLogin())
            .describeConstable().orElseThrow(()->new UserNotFoundException(authRequest.getLogin())));
    if(passwordEncoder.matches(authRequest.getPassword(), credentials.getPassword())) {
      credentials.setRefreshToken(jwtTokenProvider.generateRefreshToken(authRequest.getLogin()));
      Credential newCred = authRepository.save(credentials);
      return new AuthResponse(jwtTokenProvider.generate(String.valueOf(newCred.getId()),
              credentials.getLogin(),
              List.of("USER"),
              Map.of()), credentials.getRefreshToken());
    }
    throw new UserNotFoundException(authRequest.getLogin());  // ТУТ НАДО СДЕЛАТЬ КАСТОМНОЕ ИСКЛЮЧЕНИЕ С ТОКЕНОМ
  }

}
