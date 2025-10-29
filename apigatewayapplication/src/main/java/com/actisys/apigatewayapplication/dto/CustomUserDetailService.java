package com.actisys.apigatewayapplication.dto;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailService implements UserDetailsService {

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Здесь должна быть логика загрузки пользователя из БД или другого сервиса
    // Временно возвращаем заглушку
    return User.builder()
        .username(username)
        .password("") // пароль не используется при JWT аутентификации
        .authorities(Collections.emptyList())
        .build();
  }
}