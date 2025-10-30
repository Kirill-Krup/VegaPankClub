package com.actisys.userservice.client.impl;

import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import com.actisys.userservice.client.BillingServiceClient;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BillingServiceClientImpl implements BillingServiceClient {

  private final WebClient.Builder webClientBuilder;

  @Value("${services.billingUrl}")
  private String billingServiceUrl;

  @Override
  public Mono<SessionStatsDTO> getUserSessionStats(Long userId) {
    return webClientBuilder.build()
        .get()
        .uri(billingServiceUrl + "/api/v1/sessions/stats/{userId}", userId)
        .retrieve()
        .bodyToMono(SessionStatsDTO.class)
        .timeout(Duration.ofSeconds(3))
        .onErrorReturn(new SessionStatsDTO(0,0.0));
  }
}
