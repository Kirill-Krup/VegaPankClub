package com.actisys.common.events.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreteEvent {
    Long paymentId;
    Long orderId;
    String status;
}
