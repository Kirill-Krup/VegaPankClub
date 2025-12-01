package com.actisys.adminservice.client;

import com.actisys.adminservice.dto.PaymentInfoDTO;
import reactor.core.publisher.Mono;

public interface PaymentServiceClient {

  Mono<PaymentInfoDTO> getPaymentById(Long paymentId);
}
