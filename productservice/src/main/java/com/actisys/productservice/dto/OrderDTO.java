package com.actisys.productservice.dto;

import com.actisys.common.dto.user.UserDTO;
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

  @NotNull(message = "User is required")
  private final UserDTO userDTO;

  @NotNull(message = "Creation date is required")
  @PastOrPresent(message = "Creation date must be in the past or present")
  private final LocalDateTime createdAt;

  @NotBlank(message = "Order status is required")
  private final Status status;

  @NotNull(message = "Total cost is required")
  @DecimalMin(value = "0.01", message = "Total cost must be greater than 0")
  private final BigDecimal totalCost;

  @NotNull(message = "Order items are required")
  private final List<OrderItemDTO> orderItems;
}