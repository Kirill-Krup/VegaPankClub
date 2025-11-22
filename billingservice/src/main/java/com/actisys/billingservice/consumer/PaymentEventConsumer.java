package com.actisys.billingservice.consumer;

import com.actisys.billingservice.service.SessionService;
import com.actisys.common.events.payment.PaymentCreteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class PaymentEventConsumer {
    private final SessionService sessionService;

    @KafkaListener(topics = "UPDATE_PAYMENT_STATUS",groupId = "payment-service-group")
    public void handlePaymentStatus(PaymentCreteEvent event) {
        log.info("Payment event received: {}", event);

        sessionService.updateStatus(event.getOrderId(), event.getStatus());
    }
}
