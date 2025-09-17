package com.example.vegapank.DTO;

import java.util.List;
import lombok.Data;

@Data
public class ProductDTO {
  private Long productId;
  private String name;
  private Double price;
  private Boolean isAvailable;
  private CategoryDTO categoryDTO;
  private List<OrderItemDTO> orderItemsDTO;
}
