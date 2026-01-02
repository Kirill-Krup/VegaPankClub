package com.actisys.adminservice.dto.sessionDtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class SessionDTO {
  private final Long sessionId;

  @NotNull(message = "User ID is required")
  private final Long userId;

  @NotNull(message = "PC ID is required")
  private final Long pcId;

  private final TariffDTO tariff;

  @NotNull(message = "Start time is required")
  @PastOrPresent(message = "Start time must be in the past or present")
  private final LocalDateTime startTime;

  private final LocalDateTime endTime;

  @DecimalMin(value = "0.00", message = "Total cost must be positive")
  private final BigDecimal totalCost;

  private final SessionStatus status;
}