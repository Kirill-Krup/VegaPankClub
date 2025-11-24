package com.actisys.common.events.user;

import com.actisys.common.events.OperationType;
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
public class WithdrawEvent {
    private Long paymentId;
    private OperationType status;
}
