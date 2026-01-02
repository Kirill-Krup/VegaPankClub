package com.actisys.adminservice.dto.sessionDtos;

import com.actisys.common.user.UserDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class AllSessionDTO {

  private Long sessionId;

  private UserDTO user;

  private TariffDTO tariff;

  @NotNull(message = "Start time is required")
  @PastOrPresent(message = "Start time must be in the past or present")
  private LocalDateTime startTime;

  private LocalDateTime endTime;

  @DecimalMin(value = "0.00", message = "Total cost must be positive")
  private BigDecimal totalCost;

  private SessionStatus status;

  private PCDTO pcdto;

}
