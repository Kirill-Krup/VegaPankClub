package com.actisys.paymentservice.service.impl;

import com.actisys.common.events.payment.PaymentCreteEvent;
import com.actisys.common.user.CreateWalletEvent;
import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.mapper.PaymentMapper;
import com.actisys.paymentservice.model.Payment;
import com.actisys.paymentservice.repository.PaymentRepository;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDTO createPayment(CreatePaymentDTO createPaymentDTO) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setAmount(createPaymentDTO.getAmount());
        paymentDTO.setUserId(createPaymentDTO.getUserId());
        paymentDTO.setPaymentType(createPaymentDTO.getPaymentType());
        paymentDTO.setCreatedAt(LocalDateTime.now());
        paymentDTO.setStatus("PENDING");

        Payment save = paymentMapper.toEntity(paymentDTO);
        paymentRepository.save(save);

        CreateWalletEvent createWalletEvent = new CreateWalletEvent();
        createWalletEvent.setCost(createPaymentDTO.getAmount());
        createWalletEvent.setUserId(createPaymentDTO.getUserId());
        createWalletEvent.setPaymentId(createWalletEvent.getPaymentId());

        kafkaTemplate.send("CREATE_WALLET_EVENT", createWalletEvent);


        return paymentDTO;
    }

    @Override
    public void updateStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new RuntimeException("Payment not exist"));

        if (status.equals("ERROR")) {
            payment.setStatus("FAILED");
        } else {
            payment.setStatus("SUCCESS");
        }
        paymentRepository.save(payment);

        PaymentCreteEvent paymentCreteEvent = new PaymentCreteEvent();

        paymentCreteEvent.setPaymentId(paymentId);
        paymentCreteEvent.setStatus("SUCCESS");
        paymentCreteEvent.setOrderId(payment.getOrderId());

        kafkaTemplate.send("UPDATE_PAYMENT_STATUS", paymentId);
    }
}
