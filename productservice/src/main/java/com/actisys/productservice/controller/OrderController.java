package com.actisys.productservice.controller;

import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.dto.Status;
import com.actisys.productservice.service.OrderService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(final OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO order) {
    OrderDTO orderDTO = orderService.createOrder(order);
    return ResponseEntity.ok(orderDTO);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDTO> getOrderById(@PathVariable("id") Long id) {
    OrderDTO orderDTO = orderService.getOrderById(id);
    return ResponseEntity.ok(orderDTO);
  }

  @GetMapping("/allOrders")
  public ResponseEntity<List<OrderDTO>> getAllOrders() {
    List<OrderDTO> orderDTOList = orderService.getAllOrders();
    return ResponseEntity.ok(orderDTOList);
  }

  @GetMapping("/getOrdersByStatus")
  public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@RequestParam Status status) {
    List<OrderDTO> orderDTOList = orderService.getOrdersByStatus(status);
    return ResponseEntity.ok(orderDTOList);
  }

  @PutMapping("/status/{orderId}")
  public ResponseEntity<OrderDTO> updateOrderStatus(
      @PathVariable Long orderId,
      @RequestParam Status newStatus) {

    OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
    return ResponseEntity.ok(updatedOrder);
  }

  @DeleteMapping("/deleteOrder/{id}")
  public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return ResponseEntity.noContent().build();
  }

}
