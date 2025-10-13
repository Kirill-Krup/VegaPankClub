package com.actisys.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class OrderItemDTO {
  private final Long orderItemId;

  @NotNull(message = "Order ID is required")
  private final Long orderId;

  @NotNull(message = "Product ID is required")
  private final Long productId;

  private final String productName;

  private final BigDecimal productPrice;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private final Integer quantity;
}