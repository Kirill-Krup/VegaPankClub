package com.actisys.billingservice.dto.SessionDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateSessionDTO {
  private final Long sessionId;

  @NotNull(message = "PC ID is required")
  private final Long pcId;

  @NotNull(message = "Tariff ID is required")
  private final Long tariffId;

  @NotNull(message = "Start time is required")
  @PastOrPresent(message = "Start time must be in the past or present")
  private final LocalDateTime startTime;

  private final LocalDateTime endTime;
}
