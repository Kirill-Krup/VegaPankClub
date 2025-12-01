package com.actisys.paymentservice.controller;

import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/createPayment")
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody CreatePaymentDTO createPaymentDTO) {
        PaymentDTO paymentDTO = paymentService.createPayment(createPaymentDTO);
        return ResponseEntity.ok(paymentDTO);
    }

    @PostMapping("/createReplenishment")
    public ResponseEntity<PaymentDTO> createReplenishment(
        @RequestBody CreateReplenishment createReplenishment,
        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        PaymentDTO paymentDTO = paymentService.createReplenishment(createReplenishment, Long.parseLong(userId));
        return ResponseEntity.ok(paymentDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
}
