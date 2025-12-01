package com.actisys.adminservice.dto.paymentDtos;

import com.actisys.adminservice.dto.PaymentStatus;
import com.actisys.common.events.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}