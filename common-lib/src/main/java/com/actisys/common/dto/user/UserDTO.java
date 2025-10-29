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

  public Long getId() { return id; }
  public String getLogin() { return login; }
  public String getEmail() { return email; }
  public String getPhone() { return phone; }
  public String getFullName() { return fullName; }
  public Double getWallet() { return wallet; }
  public String getPhotoPath() { return photoPath; }
  public Integer getBonusCoins() { return bonusCoins; }
  public Timestamp getRegistrationDate() { return registrationDate; }
  public Timestamp getBirthDate() { return birthDate; }
  public Timestamp getLastLogin() { return lastLogin; }
  public Boolean getOnline() { return isOnline; }
  public Boolean getBanned() { return isBanned; }

  @JsonProperty("token")
  public String getToken() { return token; }

  @JsonProperty("role")
  public String getRole() { return role; }

  @JsonProperty
  private final Long id;

  @NotBlank
  @Size(min = 3, max = 50)
  @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String login;

  @NotBlank
  @Email
  private final String email;

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
  private final String phone;

  @Size(max = 100)
  private final String fullName;

  @Min(0)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Double wallet;

  private final String photoPath;

  @Min(0)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Integer bonusCoins;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Timestamp registrationDate;

  @Past
  private final Timestamp birthDate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Timestamp lastLogin;

  @JsonProperty(value = "online", access = JsonProperty.Access.READ_ONLY)
  private final Boolean isOnline;

  @JsonProperty(value = "banned", access = JsonProperty.Access.READ_ONLY)
  private final Boolean isBanned;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String role;

  @JsonProperty
  private transient String token;

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
      @JsonProperty("isBanned") Boolean isBanned,
      @JsonProperty(value = "role", access = JsonProperty.Access.READ_ONLY) String role,
      @JsonProperty(value = "token", access = JsonProperty.Access.READ_ONLY) String token
  ) {
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
    this.role = role;
    this.token = token;
  }

  public void setRole(String role) { this.role = role; }
  public void setToken(String token) { this.token = token; }
}

