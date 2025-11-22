package com.actisys.paymentservice.consumer;

import com.actisys.common.events.order.CreateOrderEvent;
import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class BookingEventConsumer {
    private final PaymentService paymentService;


    @KafkaListener(topics = "CREATE_BOOKING", groupId = "payment-service-group")
    public void handleCreateBooking(CreateOrderEvent createOrderEvent) {
        log.info("New booking event received: {}", createOrderEvent);

        CreatePaymentDTO createPaymentDTO = new CreatePaymentDTO();
        createPaymentDTO.setAmount(createOrderEvent.getAmount());
        createPaymentDTO.setUserId(createOrderEvent.getUserId());
        createPaymentDTO.setOrderId(createOrderEvent.getOrderId());
        paymentService.createPayment(createPaymentDTO);


    }
}