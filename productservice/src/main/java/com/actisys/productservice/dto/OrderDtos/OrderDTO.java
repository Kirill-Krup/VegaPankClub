package com.actisys.productservice.dto.OrderDtos;

import com.actisys.productservice.dto.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

  private Long id;

  private Long userId;

  private Long paymentId;

  @NotNull(message = "Creation date is required")
  @PastOrPresent(message = "Creation date must be in the past or present")
  private LocalDateTime createdAt;

  @NotNull(message = "Order status is required")
  private Status status;

  @NotNull(message = "Total cost is required")
  @DecimalMin(value = "0.01", message = "Total cost must be greater than 0")
  private BigDecimal totalCost;

  private List<OrderItemDTO> orderItems;
}
