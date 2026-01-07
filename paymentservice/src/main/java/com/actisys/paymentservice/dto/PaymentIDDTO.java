package com.actisys.paymentservice.dto;

import com.actisys.common.events.PaymentType;
import com.actisys.paymentservice.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentIDDTO {
    private Long paymentId;
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}