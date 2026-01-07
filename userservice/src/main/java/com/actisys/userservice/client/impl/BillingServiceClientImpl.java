package com.actisys.userservice.client.impl;

import com.actisys.common.clientDtos.SessionStatsDTO;
import com.actisys.userservice.client.BillingServiceClient;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BillingServiceClientImpl implements BillingServiceClient {

  private final WebClient webClient;

  public BillingServiceClientImpl(
      @Value("${services.billingUrl:http://localhost:8084}") String billingServiceUrl,
      WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder
        .baseUrl(billingServiceUrl)
        .build();

    log.info("BillingServiceClient initialized with URL: {}", billingServiceUrl);
  }

  @Override
  public Mono<SessionStatsDTO> getUserSessionStats(Long userId) {
    log.info("Requesting session stats for user: {}", userId);

    return webClient.get()
        .uri("/api/v1/sessions/stats/{userId}", userId)
        .retrieve()
        .bodyToMono(SessionStatsDTO.class)
        .timeout(Duration.ofSeconds(5))
        .doOnSuccess(stats ->
            log.info("Stats received for user {}: sessions={}, hours={}",
                userId, stats.getTotalSessions(), stats.getTotalGameHour()))
        .doOnError(error ->
            log.error("Error getting stats for user {}: {} - {}",
                userId, error.getClass().getSimpleName(), error.getMessage(), error))
        .onErrorReturn(new SessionStatsDTO(0, 0.0));
  }
}
