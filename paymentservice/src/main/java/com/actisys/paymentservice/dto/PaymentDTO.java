package com.actisys.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private String paymentType;
    private String status;
    private LocalDateTime createdAt;
}