package com.example.vegapank.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  private String login;
  private String password;
  private String phone;

  @Column(name = "full_name")
  private String fullName;

  private String email;
  private Double wallet;

  @Column(name = "photo_path")
  private String photoPath;

  @Column(name = "bonus_coins")
  private Integer bonusCoins;

  @Column(name = "registration_date")
  private LocalDateTime registrationDate;

  @Column(name = "birthday_date")
  private LocalDate birthdayDate;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @Column(name = "is_online")
  private Boolean isOnline;

  @Column(name = "is_banned")
  private Boolean isBanned;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Session> sessions;
}
