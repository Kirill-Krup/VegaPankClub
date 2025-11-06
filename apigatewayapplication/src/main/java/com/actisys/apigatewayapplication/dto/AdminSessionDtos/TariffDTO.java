package com.actisys.apigatewayapplication.dto.AdminSessionDtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TariffDTO {
  private final Long tariffId;

  @NotBlank(message = "Tariff name is required")
  @Size(min = 2, max = 100, message = "Tariff name must be between 2 and 100 characters")
  private final String name;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  private final BigDecimal price;

  @NotNull(message = "VIP status is required")
  private final boolean isVip;

  @NotNull(message = "Hours is required")
  private final int hours;
}