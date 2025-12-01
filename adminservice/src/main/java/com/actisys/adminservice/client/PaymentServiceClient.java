package com.actisys.adminservice.client;

import com.actisys.adminservice.dto.PaymentDTO;
import reactor.core.publisher.Mono;

public interface PaymentServiceClient {

  Mono<PaymentDTO> getPaymentById(Long paymentId);
}
