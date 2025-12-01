package com.actisys.paymentservice.service;

import com.actisys.common.events.OperationType;
import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.repository.PaymentRepository;
import org.springframework.kafka.core.KafkaTemplate;

public interface PaymentService {
    PaymentDTO createPayment(CreatePaymentDTO createPaymentDTO);

    void updateStatus(Long paymentId, OperationType status);

    PaymentDTO createReplenishment(CreateReplenishment createReplenishment, Long userId);

  PaymentDTO getPaymentById(Long id);
}
