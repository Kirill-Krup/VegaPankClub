package com.actisys.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Getter
@RequiredArgsConstructor
public class UserDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Long id;

    @NotBlank(message = "Login cannot be blank")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Login can only contain letters, numbers, underscores and hyphens")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final String login;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phone;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @Min(value = 0, message = "Wallet cannot be negative")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final double wallet;

    private String photoPath;

    @Min(value = 0, message = "Bonus coins cannot be negative")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final int bonusCoins;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Timestamp registrationDate;

    @Past(message = "Birth date must be in the past")
    private Timestamp birthDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Timestamp lastLogin;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final boolean isOnline;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final boolean isBanned;
}