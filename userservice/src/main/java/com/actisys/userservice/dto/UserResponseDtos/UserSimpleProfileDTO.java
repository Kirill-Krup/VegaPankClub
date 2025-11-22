package com.actisys.userservice.dto.UserResponseDtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSimpleProfileDTO {
  private final String login;
  private final BigDecimal wallet;
  private final String photoPath;
  private final boolean isBanned;
  private final int role;

  @JsonCreator
  public UserSimpleProfileDTO(
      @JsonProperty("login") String login,
      @JsonProperty("wallet") BigDecimal wallet,
      @JsonProperty("photoPath") String photoPath,
      @JsonProperty("banned") boolean isBanned,
      @JsonProperty("role") int role) {
    this.login = login;
    this.wallet = wallet;
    this.photoPath = photoPath;
    this.isBanned = isBanned;
    this.role = role;
  }
}
