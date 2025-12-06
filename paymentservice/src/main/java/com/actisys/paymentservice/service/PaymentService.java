package com.actisys.paymentservice.service;

import com.actisys.common.events.OperationType;
import com.actisys.paymentservice.dto.CreatePaymentDTO;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;

public interface PaymentService {

  /**
   * Creates new payment record and publishes CreateWalletEvent to Kafka.
   * Sets initial CREATED status and stores payment details for order processing.
   *
   * @param createPaymentDTO payment data including amount, userId, orderId and type
   * @return created PaymentDTO with generated ID
   */
  PaymentDTO createPayment(CreatePaymentDTO createPaymentDTO);

  /**
   * Updates payment status based on operation result and publishes events if needed.
   * Handles REFUNDED status separately, sends topic-specific Kafka events for bookings/bar buys.
   *
   * @param paymentId identifier of payment to update
   * @param status new operation status (SUCCESS, ERROR, REFUNDED)
   */
  void updateStatus(Long paymentId, OperationType status);

  /**
   * Creates wallet replenishment payment with REPLENISHMENT type and orderId=0.
   * Publishes CREATE_WALLET_REPLENISHMENT_EVENT to Kafka for balance update.
   *
   * @param createReplenishment replenishment amount data
   * @param userId ID of user performing replenishment
   * @return created replenishment PaymentDTO
   */
  PaymentDTO createReplenishment(CreateReplenishment createReplenishment, Long userId);

  /**
   * Retrieves single payment record by identifier.
   *
   * @param id payment identifier
   * @return PaymentDTO with full payment details
   */
  PaymentDTO getPaymentById(Long id);
}
