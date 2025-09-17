package com.example.vegapank.DTO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderDTO {
  private Long orderId;
  private LocalDateTime createdAt;
  private String status;
  private Double totalCost;
  private List<OrderItemDTO> orderItemsDTO;
}
