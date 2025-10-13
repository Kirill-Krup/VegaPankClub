package com.actisys.userservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String login;

  @Column(unique = true)
  private String email;

  private String phone;

  @Column(name = "full_name")
  private String fullName;

  private double wallet;

  @Column(name = "photo_path")
  private String photoPath;

  @Column(name = "bonus_coins")
  private int bonusCoins;

  @Column(name = "registration_date")
  private Timestamp registrationDate;

  @Column(name = "birthday_date")
  private Timestamp birthDate;

  @Column(name = "last_login")
  private Timestamp lastLogin;

  @Column(name = "is_online")
  private boolean isOnline;

  @Column(name = "is_banned")
  private boolean isBanned;
}
