package com.actisys.adminservice.client.impl;

import com.actisys.adminservice.client.BillingServiceClient;
import com.actisys.adminservice.config.BillingServiceProperties;
import com.actisys.adminservice.config.ProductServiceProperties;
import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class BillingServiceClientImpl implements BillingServiceClient {

  private final WebClient webClient;
  private final BillingServiceProperties properties;

  public BillingServiceClientImpl(
      BillingServiceProperties properties,
      WebClient.Builder webClientBuilder) {
    this.properties = properties;
    this.webClient = webClientBuilder
        .baseUrl(properties.getHost())
        .build();
  }

  @Override
  public Mono<List<SessionDTO>> getAllSessions() {
    return webClient.get()
        .uri(properties.getEndpoints().getGetAllSessions())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<SessionDTO>>() {})
        .timeout(Duration.ofSeconds(5));
  }
}
