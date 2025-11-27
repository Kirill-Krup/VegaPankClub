package com.actisys.paymentservice.UnitTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.actisys.common.events.OperationType;
import com.actisys.common.events.PaymentType;
import com.actisys.common.events.user.CreateWalletEvent;
import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.exception.PaymentNotFoundException;
import com.actisys.paymentservice.mapper.PaymentMapper;
import com.actisys.paymentservice.model.Payment;
import com.actisys.paymentservice.model.PaymentStatus;
import com.actisys.paymentservice.repository.PaymentRepository;
import com.actisys.paymentservice.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

class PaymentServiceImplTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Mock
  private PaymentMapper paymentMapper;

  @InjectMocks
  private PaymentServiceImpl paymentService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // ---------- createPayment ----------

  @Test
  void createPayment_shouldSavePaymentAndSendCreateWalletEvent() {

    CreatePaymentDTO request = new CreatePaymentDTO();
    request.setAmount(BigDecimal.valueOf(100));
    request.setUserId(10L);
    request.setOrderId(20L);
    request.setPaymentType(PaymentType.BOOKING);

    PaymentDTO mappedDto = new PaymentDTO();
    mappedDto.setAmount(request.getAmount());
    mappedDto.setUserId(request.getUserId());
    mappedDto.setOrderId(request.getOrderId());
    mappedDto.setPaymentType(request.getPaymentType());
    mappedDto.setStatus(PaymentStatus.CREATED);
    mappedDto.setCreatedAt(LocalDateTime.now());

    Payment entity = new Payment();

    when(paymentMapper.toEntity(any(PaymentDTO.class))).thenReturn(entity);
    when(paymentRepository.save(any(Payment.class))).thenReturn(entity);

    PaymentDTO result = paymentService.createPayment(request);

    assertEquals(request.getAmount(), result.getAmount());
    assertEquals(request.getUserId(), result.getUserId());
    assertEquals(request.getOrderId(), result.getOrderId());
    assertEquals(PaymentStatus.CREATED, result.getStatus());

    verify(paymentMapper).toEntity(any(PaymentDTO.class));
    verify(paymentRepository).save(entity);

    ArgumentCaptor<CreateWalletEvent> eventCaptor =
        ArgumentCaptor.forClass(CreateWalletEvent.class);
    verify(kafkaTemplate).send(eq("CREATE_WALLET_EVENT"), eventCaptor.capture());

    CreateWalletEvent sentEvent = eventCaptor.getValue();
    assertEquals(request.getAmount(), sentEvent.getCost());
    assertEquals(request.getUserId(), sentEvent.getUserId());
    assertEquals(request.getPaymentType(), sentEvent.getPaymentType());
  }

  // ---------- updateStatus ----------

  @Test
  void updateStatus_whenPaymentNotFound_shouldThrow() {

    when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(PaymentNotFoundException.class,
        () -> paymentService.updateStatus(1L, OperationType.SUCCESS));

    verify(paymentRepository, never()).save(any());
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void updateStatus_whenRefunded_shouldSetRefundedAndNotSendKafka() {

    Payment payment = new Payment();
    payment.setPaymentId(1L);
    payment.setOrderId(123L);
    payment.setStatus(PaymentStatus.CREATED);

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    paymentService.updateStatus(1L, OperationType.REFUNDED);

    assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    verify(paymentRepository).save(payment);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void updateStatus_whenOrderIdZero_shouldUpdateStatusWithoutKafka() {

    Payment payment = new Payment();
    payment.setPaymentId(1L);
    payment.setOrderId(0L);
    payment.setPaymentType(PaymentType.REPLENISHMENT);
    payment.setStatus(PaymentStatus.CREATED);

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    paymentService.updateStatus(1L, OperationType.SUCCESS);

    assertEquals(PaymentStatus.PAID, payment.getStatus());
    verify(paymentRepository).save(payment);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void updateStatus_whenBookingPayment_shouldSendBookingTopic() {

    Payment payment = new Payment();
    payment.setPaymentId(1L);
    payment.setOrderId(99L);
    payment.setPaymentType(PaymentType.BOOKING);
    payment.setStatus(PaymentStatus.CREATED);

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    paymentService.updateStatus(1L, OperationType.SUCCESS);

    assertEquals(PaymentStatus.PAID, payment.getStatus());
    verify(paymentRepository).save(payment);
    verify(kafkaTemplate).send("UPDATE_PAYMENT_STATUS_FOR_BOOKING", 1L);
    verify(kafkaTemplate, never())
        .send(eq("UPDATE_PAYMENT_STATUS_FOR_BAR_BUY"), any());
  }

  @Test
  void updateStatus_whenBarBuyPayment_shouldSendBarTopic() {

    Payment payment = new Payment();
    payment.setPaymentId(1L);
    payment.setOrderId(99L);
    payment.setPaymentType(PaymentType.BAR_BUY);
    payment.setStatus(PaymentStatus.CREATED);

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    paymentService.updateStatus(1L, OperationType.SUCCESS);

    assertEquals(PaymentStatus.PAID, payment.getStatus());
    verify(paymentRepository).save(payment);
    verify(kafkaTemplate).send("UPDATE_PAYMENT_STATUS_FOR_BAR_BUY", 1L);
    verify(kafkaTemplate, never())
        .send(eq("UPDATE_PAYMENT_STATUS_FOR_BOOKING"), any());
  }

  @Test
  void updateStatus_whenError_shouldSetFailed() {

    Payment payment = new Payment();
    payment.setPaymentId(1L);
    payment.setOrderId(99L);
    payment.setPaymentType(PaymentType.BOOKING);
    payment.setStatus(PaymentStatus.CREATED);

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

    paymentService.updateStatus(1L, OperationType.ERROR);

    assertEquals(PaymentStatus.FAILED, payment.getStatus());
    verify(paymentRepository).save(payment);
    // Kafka всё равно пошлётся, т.к. orderId != 0; проверяем, что топик верный
    verify(kafkaTemplate).send("UPDATE_PAYMENT_STATUS_FOR_BOOKING", 1L);
  }

  // ---------- createReplenishment ----------

  @Test
  void createReplenishment_shouldSavePaymentAndSendWalletReplenishmentEvent() {

    CreateReplenishment request = new CreateReplenishment();
    request.setReplenishmentAmount(BigDecimal.valueOf(50));
    Long userId = 77L;

    Payment saved = new Payment();
    saved.setPaymentId(5L);
    saved.setAmount(request.getReplenishmentAmount());
    saved.setUserId(userId);
    saved.setOrderId(0L);
    saved.setStatus(PaymentStatus.CREATED);
    saved.setPaymentType(PaymentType.REPLENISHMENT);
    saved.setCreatedAt(LocalDateTime.now());

    PaymentDTO dto = new PaymentDTO();
    dto.setAmount(saved.getAmount());
    dto.setUserId(saved.getUserId());
    dto.setOrderId(saved.getOrderId());
    dto.setStatus(saved.getStatus());
    dto.setPaymentType(saved.getPaymentType());
    dto.setCreatedAt(saved.getCreatedAt());

    when(paymentRepository.save(any(Payment.class))).thenReturn(saved);
    when(paymentMapper.toDto(any(Payment.class))).thenReturn(dto);

    PaymentDTO result = paymentService.createReplenishment(request, userId);

    assertEquals(PaymentType.REPLENISHMENT, result.getPaymentType());
    assertEquals(PaymentStatus.CREATED, result.getStatus());
    assertEquals(userId, result.getUserId());
    assertEquals(0L, result.getOrderId());
    assertEquals(request.getReplenishmentAmount(), result.getAmount());

    verify(paymentRepository).save(any(Payment.class));
    verify(paymentMapper).toDto(any(Payment.class));

    ArgumentCaptor<CreateWalletEvent> eventCaptor =
        ArgumentCaptor.forClass(CreateWalletEvent.class);
    verify(kafkaTemplate).send(eq("CREATE_WALLET_REPLENISHMENT_EVENT"),
        eventCaptor.capture());

    CreateWalletEvent sentEvent = eventCaptor.getValue();
    assertEquals(request.getReplenishmentAmount(), sentEvent.getCost());
    assertEquals(userId, sentEvent.getUserId());
    assertEquals(saved.getPaymentId(), sentEvent.getPaymentId());
  }

}
