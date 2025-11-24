package com.actisys.common.events.user;

import com.actisys.common.events.PaymentType;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateWalletEvent {
    private Long userId;
    private Long paymentId;
    private BigDecimal cost;
    private PaymentType paymentType;
}
