package com.actisys.billingservice.dto.TariffDtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
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

  @JsonCreator
  public TariffDTO(
      @JsonProperty("tariffId") Long tariffId,
      @JsonProperty("name") String name,
      @JsonProperty("price") BigDecimal price,
      @JsonProperty("vip") boolean isVip,
      @JsonProperty("hours") int hours) {
    this.tariffId = tariffId;
    this.name = name;
    this.price = price;
    this.isVip = isVip;
    this.hours = hours;
  }
}