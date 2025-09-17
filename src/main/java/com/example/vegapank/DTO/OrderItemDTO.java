package com.example.vegapank.DTO;

import lombok.Data;

@Data

public class OrderItemDTO {
  private Long orderItemId;
  private Integer quantity;
  private OrderDTO orderDTO;
  private ProductDTO productDTO;
}
