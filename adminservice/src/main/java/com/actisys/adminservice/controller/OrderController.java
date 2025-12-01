package com.actisys.adminservice.controller;

import com.actisys.adminservice.dto.orderDtos.AllOrderDTO;
import com.actisys.adminservice.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
public class OrderController {

  private final OrderService orderService;

  @GetMapping("/getAllOrders")
  public Mono<ResponseEntity<List<AllOrderDTO>>> allOrders(){
    return orderService.getAllOrders()
        .map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }
}
