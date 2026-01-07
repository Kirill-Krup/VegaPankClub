package com.actisys.productservice.dto.OrderDtos;

import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class OrderItemDTO {
  private Long orderItemId;

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Product is required")
  private ProductDTO productDTO;

  @NotNull(message = "Product price is required")
  @Negative(message = "Product price can not be negative")
  private BigDecimal productPrice;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity;
}