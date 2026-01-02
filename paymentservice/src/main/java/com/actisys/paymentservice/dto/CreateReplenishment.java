package com.actisys.paymentservice.dto;

import com.actisys.common.events.PaymentType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReplenishment {
  private BigDecimal replenishmentAmount;
}
