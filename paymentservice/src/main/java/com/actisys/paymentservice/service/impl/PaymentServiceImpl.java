package com.actisys.paymentservice.service.impl;

import com.actisys.common.events.OperationType;
import com.actisys.common.events.PaymentType;
import com.actisys.common.events.payment.CreatePaymentEvent;
import com.actisys.common.events.user.CreateWalletEvent;
import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.exception.PaymentNotFoundException;
import com.actisys.paymentservice.mapper.PaymentMapper;
import com.actisys.paymentservice.model.Payment;
import com.actisys.paymentservice.model.PaymentStatus;
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
        paymentDTO.setOrderId(createPaymentDTO.getOrderId());
        paymentDTO.setPaymentType(createPaymentDTO.getPaymentType());
        paymentDTO.setCreatedAt(LocalDateTime.now());
        paymentDTO.setStatus(PaymentStatus.CREATED);

        Payment save = paymentMapper.toEntity(paymentDTO);
        paymentRepository.save(save);

        CreateWalletEvent createWalletEvent = new CreateWalletEvent();
        createWalletEvent.setCost(createPaymentDTO.getAmount());
        createWalletEvent.setUserId(createPaymentDTO.getUserId());
        createWalletEvent.setPaymentId(createWalletEvent.getPaymentId());
        createWalletEvent.setPaymentType(createPaymentDTO.getPaymentType());

        kafkaTemplate.send("CREATE_WALLET_EVENT", createWalletEvent);


        return paymentDTO;
    }

    @Override
    public void updateStatus(Long paymentId, OperationType status) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
            new PaymentNotFoundException(paymentId));
        if(OperationType.REFUNDED.equals(status)){
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            return;
        }
        if(payment.getOrderId()==0){
            payment.setStatus(statusHandler(status));
            paymentRepository.save(payment);
            return;
        }
        payment.setStatus(statusHandler(status));
        paymentRepository.save(payment);
        CreatePaymentEvent paymentCreteEvent = new CreatePaymentEvent();
        paymentCreteEvent.setPaymentId(paymentId);
        paymentCreteEvent.setStatus(OperationType.SUCCESS);
        paymentCreteEvent.setOrderId(payment.getOrderId());
        if(payment.getPaymentType().equals(PaymentType.BOOKING)){
            kafkaTemplate.send("UPDATE_PAYMENT_STATUS_FOR_BOOKING", paymentId);
        } else if(payment.getPaymentType().equals(PaymentType.BAR_BUY)){
            kafkaTemplate.send("UPDATE_PAYMENT_STATUS_FOR_BAR_BUY", paymentId);
        }

    }

    @Override
    public PaymentDTO createReplenishment(CreateReplenishment createReplenishment, Long userId) {
        Payment payment = new Payment();
        payment.setAmount(createReplenishment.getReplenishmentAmount());
        payment.setUserId(userId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setOrderId(0L);
        payment.setPaymentType(PaymentType.REPLENISHMENT);
        Payment savedPayment = paymentRepository.save(payment);
        CreateWalletEvent createWalletEvent = new CreateWalletEvent();
        createWalletEvent.setCost(savedPayment.getAmount());
        createWalletEvent.setUserId(savedPayment.getUserId());
        createWalletEvent.setPaymentId(savedPayment.getPaymentId());
        kafkaTemplate.send("CREATE_WALLET_REPLENISHMENT_EVENT", createWalletEvent);
        return paymentMapper.toDto(payment);
    }

    private PaymentStatus statusHandler(OperationType operationType){
        return operationType ==  OperationType.ERROR ? PaymentStatus.FAILED : PaymentStatus.PAID;
    }
}
