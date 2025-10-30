package com.actisys.userservice.dto.UserResponseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserSimpleProfileDTO {
  private final String login;
  private final double wallet;
  private final String photoPath;
  private final boolean isBanned;
}
