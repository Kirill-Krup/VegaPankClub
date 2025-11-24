package com.actisys.common.events.payment;

import com.actisys.common.events.OperationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentEvent {
    private Long paymentId;
    private Long orderId;
    private OperationType status;
}
