package com.actisys.paymentservice.consumer;

import com.actisys.common.events.user.WithdrawEvent;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class WithdrawEventConsumer {
    private final PaymentService paymentService;

    @KafkaListener(topics = "WALLET_EVENT", groupId = "user-service-group")
    public void handleWithDraw(WithdrawEvent event) {
        log.info("WALLET_EVENT:{}", event);
        paymentService.updateStatus(event.getPaymentId(), event.getStatus());
    }
}
