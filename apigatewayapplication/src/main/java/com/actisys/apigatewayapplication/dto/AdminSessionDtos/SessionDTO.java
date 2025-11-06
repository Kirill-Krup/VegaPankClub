package com.actisys.apigatewayapplication.dto.AdminSessionDtos;

import com.actisys.common.dto.user.UserDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionDTO {
  private final Long sessionId;

  @NotNull(message = "User ID is required")
  private final UserDTO user;

  @NotNull(message = "PC ID is required")
  private final PCDTO pcDTO;

  private final TariffDTO tariff;

  @NotNull(message = "Start time is required")
  @PastOrPresent(message = "Start time must be in the past or present")
  private final LocalDateTime startTime;

  private final LocalDateTime endTime;

  @DecimalMin(value = "0.00", message = "Total cost must be positive")
  private final BigDecimal totalCost;

  @NotBlank(message = "Session status is required")
  @Size(max = 50, message = "Status must not exceed 50 characters")
  private final String status;
}