package com.actisys.common.user;

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
}
