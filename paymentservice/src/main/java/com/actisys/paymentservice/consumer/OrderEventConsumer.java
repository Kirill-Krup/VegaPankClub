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

}