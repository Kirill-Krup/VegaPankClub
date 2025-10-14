package com.actisys.userservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UpdateCoinsRequest {
  @NotNull(message = "Coins amount is required")
  @Min(value = 0, message = "Coins amount must be positive")
  private int coins;
}
