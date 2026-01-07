package com.actisys.adminservice.client;

import com.actisys.adminservice.dto.PaymentInfoDTO;
import com.actisys.adminservice.dto.paymentDtos.PaymentIDDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PaymentServiceClient {

  Mono<List<PaymentIDDTO>> getAllPayments();

  Mono<PaymentInfoDTO> getPaymentById(Long paymentId);
}
