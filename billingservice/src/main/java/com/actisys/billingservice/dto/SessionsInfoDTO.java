package com.actisys.billingservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SessionsInfoDTO {
  private final Long pcId;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
}
