package com.actisys.adminservice.client.impl;

import com.actisys.adminservice.client.BillingServiceClient;
import com.actisys.adminservice.config.ServiceProperties.BillingServiceProperties;
import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
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

  @Override
  public Mono<SessionDTO> getSessionById(Long orderId) {
    return webClient.get()
        .uri(properties.getEndpoints().getGetSession(), orderId)
        .retrieve()
        .bodyToMono(SessionDTO.class)
        .timeout(Duration.ofSeconds(3));
  }
}
