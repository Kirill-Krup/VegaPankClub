package com.actisys.common.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;

public class UserDTO {

  public Long getId() {
    return id;
  }

  public String getLogin() {
    return login;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getFullName() {
    return fullName;
  }

  public Double getWallet() {
    return wallet;
  }

  public String getPhotoPath() {
    return photoPath;
  }

  public Integer getBonusCoins() {
    return bonusCoins;
  }

  public Timestamp getRegistrationDate() {
    return registrationDate;
  }

  public Timestamp getBirthDate() {
    return birthDate;
  }

  public Timestamp getLastLogin() {
    return lastLogin;
  }

  public Boolean getOnline() {
    return isOnline;
  }

  public Boolean getBanned() {
    return isBanned;
  }

  @JsonProperty
  private final Long id;

  @NotBlank(message = "Login cannot be blank")
  @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
  @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Login can only contain letters, numbers, underscores and hyphens")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String login;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email must be valid")
  private final String email;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
  private final String phone;

  @Size(max = 100, message = "Full name cannot exceed 100 characters")
  private final String fullName;

  @Min(value = 0, message = "Wallet cannot be negative")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Double wallet;

  private final String photoPath;

  @Min(value = 0, message = "Bonus coins cannot be negative")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Integer bonusCoins;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Timestamp registrationDate;

  @Past(message = "Birth date must be in the past")
  private final Timestamp birthDate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Timestamp lastLogin;

  @JsonProperty(value = "online",access = JsonProperty.Access.READ_ONLY)
  private final Boolean isOnline;

  @JsonProperty(value = "banned", access = JsonProperty.Access.READ_ONLY)
  private final Boolean isBanned;

  @JsonCreator
  public UserDTO(
      @JsonProperty("id") Long id,
      @JsonProperty("login") String login,
      @JsonProperty("email") String email,
      @JsonProperty("phone") String phone,
      @JsonProperty("fullName") String fullName,
      @JsonProperty("wallet") Double wallet,
      @JsonProperty("photoPath") String photoPath,
      @JsonProperty("bonusCoins") Integer bonusCoins,
      @JsonProperty("registrationDate") Timestamp registrationDate,
      @JsonProperty("birthDate") Timestamp birthDate,
      @JsonProperty("lastLogin") Timestamp lastLogin,
      @JsonProperty("isOnline") Boolean isOnline,
      @JsonProperty("isBanned") Boolean isBanned) {
    this.id = id;
    this.login = login;
    this.email = email;
    this.phone = phone;
    this.fullName = fullName;
    this.wallet = wallet;
    this.photoPath = photoPath;
    this.bonusCoins = bonusCoins;
    this.registrationDate = registrationDate;
    this.birthDate = birthDate;
    this.lastLogin = lastLogin;
    this.isOnline = isOnline;
    this.isBanned = isBanned;
  }
}