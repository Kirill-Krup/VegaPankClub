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
public class OrderEventConsumer {

  private final PaymentService paymentService;

  @KafkaListener(topics = "CREATE_ORDER_EVENT", groupId = "payment-service-group")
  private void handleCreateOrderEvent(CreateOrderEvent event) {
    log.info("Received CreateOrderEvent: {}", event);
    CreatePaymentDTO paymentDTO = new CreatePaymentDTO();
    paymentDTO.setOrderId(event.getOrderId());
    paymentDTO.setUserId(event.getUserId());
    paymentDTO.setAmount(event.getAmount());
    paymentDTO.setPaymentType(event.getPaymentType());

    paymentService.createPayment(paymentDTO);
  }

}