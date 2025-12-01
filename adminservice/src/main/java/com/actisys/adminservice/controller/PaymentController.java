package com.actisys.adminservice.controller;

import com.actisys.adminservice.dto.paymentDtos.AllPaymentDTO;
import com.actisys.adminservice.service.PaymentService;
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
@RequestMapping("/api/v1/admin/payments")
public class PaymentController {

  private final PaymentService paymentService;

  @GetMapping("/getAllPayments")
  public Mono<ResponseEntity<List<AllPaymentDTO>>> allPayments(){
    return paymentService.getAllPayments()
        .map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }
}
