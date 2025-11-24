package com.actisys.productservice.consumer;

import com.actisys.common.events.payment.CreatePaymentEvent;
import com.actisys.productservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {
  private final OrderService orderService;

  @KafkaListener(topics = "UPDATE_PAYMENT_STATUS_FOR_BAR_BUY", groupId = "product-service-group")
  public void handleCreatePayment(CreatePaymentEvent event) {
    log.info("CreatePayment event received");
    orderService.updateStatusByEvent(event);
  }
}
