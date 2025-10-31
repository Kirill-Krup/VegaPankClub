package com.actisys.userservice.dto.UserResponseDtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSimpleProfileDTO {
  private final String login;
  private final double wallet;
  private final String photoPath;
  private final boolean isBanned;
  private final int role;

  @JsonCreator
  public UserSimpleProfileDTO(
      @JsonProperty("login") String login,
      @JsonProperty("wallet") double wallet,
      @JsonProperty("photoPath") String photoPath,
      @JsonProperty("isBanned") boolean isBanned,
      @JsonProperty("role") int role) {
    this.login = login;
    this.wallet = wallet;
    this.photoPath = photoPath;
    this.isBanned = isBanned;
    this.role = role;
  }
}
