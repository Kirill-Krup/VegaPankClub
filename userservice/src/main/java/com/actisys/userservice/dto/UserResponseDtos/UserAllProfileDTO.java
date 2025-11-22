package com.actisys.userservice.dto.UserResponseDtos;

import com.actisys.common.clientDtos.SessionStatsDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
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

  @JsonCreator
  public UserAllProfileDTO(
      @JsonProperty("login") String login,
      @JsonProperty("fullName") String fullName,
      @JsonProperty("email") String email,
      @JsonProperty("phone") String phone,
      @JsonProperty("wallet") double wallet,
      @JsonProperty("photoPath") String photoPath,
      @JsonProperty("bonusCoins") int bonusCoins,
      @JsonProperty("registrationDate") Timestamp registrationDate,
      @JsonProperty("birthDate") Timestamp birthDate,
      @JsonProperty("sessionStats") SessionStatsDTO sessionStats) {
    this.login = login;
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
    this.wallet = wallet;
    this.photoPath = photoPath;
    this.bonusCoins = bonusCoins;
    this.registrationDate = registrationDate;
    this.birthDate = birthDate;
    this.sessionStats = sessionStats;
  }
}
