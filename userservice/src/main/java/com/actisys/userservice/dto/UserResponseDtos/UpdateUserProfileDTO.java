package com.actisys.userservice.dto.UserResponseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UpdateUserProfileDTO {
  private final String fullName;
  private final String email;
  private final String phone;
}
