package com.actisys.common.events.user;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundMoneyEvent {
  private Long userId;
  private Long paymentId;
  private BigDecimal amount;
}
