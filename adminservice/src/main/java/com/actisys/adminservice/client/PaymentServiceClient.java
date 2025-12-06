package com.actisys.adminservice.client;

import com.actisys.adminservice.dto.PaymentInfoDTO;
import com.actisys.adminservice.dto.paymentDtos.PaymentDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface PaymentServiceClient {

  Mono<List<PaymentDTO>> getAllPayments();

  Mono<PaymentInfoDTO> getPaymentById(Long paymentId);
}
