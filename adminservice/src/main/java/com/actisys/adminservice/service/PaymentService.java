package com.actisys.adminservice.service;

import com.actisys.adminservice.dto.paymentDtos.AllPaymentDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface PaymentService {

  Mono<List<AllPaymentDTO>> getAllPayments();
}
