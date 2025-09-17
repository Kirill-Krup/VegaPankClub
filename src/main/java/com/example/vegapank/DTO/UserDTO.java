package com.example.vegapank.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserDTO {
  private Long id;
  private String login;
  private String phone;
  private String fullName;
  private String email;
  private Double wallet;
  private String photoPath;
  private Double bonusCoins;
  private LocalDateTime registrationDate;
  private LocalDate birthdayDate;
}
