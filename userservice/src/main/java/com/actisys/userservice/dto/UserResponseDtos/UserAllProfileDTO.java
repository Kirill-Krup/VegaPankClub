package com.actisys.userservice.dto.UserResponseDtos;

import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserAllProfileDTO {
  private final String login;
  private final String fullName;
  private final String email;
  private final String phone;
  private final double wallet;
  private final String photoPath;
  private final int bonusCoins;
  private final Timestamp registrationDate;
  private final Timestamp birthDate;
  private SessionStatsDTO sessionStats;
}
