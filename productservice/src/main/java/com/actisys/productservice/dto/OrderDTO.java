package com.actisys.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class OrderDTO {
  private final Long orderId;

  @NotNull(message = "User ID is required")
  private final Long userId;

  @NotNull(message = "Creation date is required")
  @PastOrPresent(message = "Creation date must be in the past or present")
  private final LocalDateTime createdAt;

  @NotBlank(message = "Order status is required")
  private final String status;

  @NotNull(message = "Total cost is required")
  @DecimalMin(value = "0.01", message = "Total cost must be greater than 0")
  private final BigDecimal totalCost;

  @NotNull(message = "Order items are required")
  private final List<OrderItemDTO> orderItems;
}