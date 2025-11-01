package com.actisys.common.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private String login;
  private String email;
  private String phone;
  private String fullName;
  private Double wallet;
  private String photoPath;
  private Integer bonusCoins;
  private Timestamp registrationDate;
  private Timestamp birthDate;
  private Timestamp lastLogin;
  private Boolean online;
  private Boolean banned;
  private String role;
}
