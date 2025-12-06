package com.actisys.adminservice.client.impl;

import com.actisys.adminservice.client.PaymentServiceClient;
import com.actisys.adminservice.config.ServiceProperties.PaymentServiceProperties;
import com.actisys.adminservice.dto.PaymentInfoDTO;
import com.actisys.adminservice.dto.paymentDtos.PaymentDTO;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentServiceClientImpl implements PaymentServiceClient {

  private final WebClient webClient;
  private final PaymentServiceProperties properties;

  public PaymentServiceClientImpl(
      PaymentServiceProperties properties,
      WebClient.Builder webClientBuilder) {
    this.properties = properties;
    this.webClient = webClientBuilder
        .baseUrl(properties.getHost())
        .build();
  }

  @Override
  public Mono<List<PaymentDTO>> getAllPayments() {
    return null;
  }

  @Override
  public Mono<PaymentInfoDTO> getPaymentById(Long paymentId) {
    return webClient.get()
        .uri(properties.getEndpoints().getGetPayment(), paymentId)
        .retrieve()
        .bodyToMono(PaymentInfoDTO.class)
        .timeout(Duration.ofSeconds(5));
  }
}
