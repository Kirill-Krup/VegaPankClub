package com.actisys.userservice.client.impl;

import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import com.actisys.userservice.client.BillingServiceClient;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
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
        .doOnSuccess(stats -> log.debug("Stats received for user {}: {}", userId, stats))
        .doOnError(error -> log.error("Error getting stats for user {}: {}", userId, error.getMessage()))
        .onErrorReturn(new SessionStatsDTO());
  }
}
