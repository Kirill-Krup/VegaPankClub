package com.actisys.billingservice.dto.SessionDtos;

import com.actisys.billingservice.dto.TariffDtos.TariffDTO;
import com.actisys.billingservice.model.SessionStatus;
import com.actisys.common.clientDtos.PcResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class needs to share
 * data about history or
 * future user's sessions
 * */

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class SessionResponseDto {
  private final Long sessionId;
  private PcResponseDTO pc;
  private final TariffDTO tariff;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
  private final BigDecimal totalCost;
  private final SessionStatus status;
}
