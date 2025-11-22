package com.actisys.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentDTO {
    private Long userId;
    private BigDecimal amount;
    private Long orderId;
    private String paymentType;
}
