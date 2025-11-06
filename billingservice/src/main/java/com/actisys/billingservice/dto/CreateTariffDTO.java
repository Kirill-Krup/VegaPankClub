package com.actisys.billingservice.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateTariffDTO {
  private final String name;
  private final BigDecimal price;
  private final boolean isVip;
  private final int hours;
}
