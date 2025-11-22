package com.actisys.paymentservice.controller;

import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    }
}
