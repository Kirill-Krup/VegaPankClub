package com.actisys.userservice.dto;

import com.actisys.common.dto.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
  private UserDTO user;
  private String token;

  public AuthResponse(UserDTO user, String token) {
    this.user = user;
    this.token = token;
  }
}