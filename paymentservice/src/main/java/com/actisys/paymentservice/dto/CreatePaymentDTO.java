package com.actisys.paymentservice.dto;

import com.actisys.common.events.PaymentType;
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
    private PaymentType paymentType;
}
