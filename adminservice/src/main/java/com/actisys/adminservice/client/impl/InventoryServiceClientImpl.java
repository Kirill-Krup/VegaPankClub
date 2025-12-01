package com.actisys.adminservice.client.impl;

import com.actisys.adminservice.client.InventoryServiceClient;
import com.actisys.adminservice.config.InventoryServiceProperties;
import com.actisys.adminservice.dto.sessionDtos.PCDTO;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryServiceClientImpl implements InventoryServiceClient {

  private final WebClient webClient;
  private final InventoryServiceProperties properties;

  public InventoryServiceClientImpl(
      InventoryServiceProperties properties,
      WebClient.Builder webClientBuilder) {
    this.properties = properties;
    this.webClient = webClientBuilder
        .baseUrl(properties.getHost())
        .build();
  }

  @Override
  public Mono<PCDTO> getPcById(Long pcId) {
    return webClient.get()
        .uri(properties.getEndpoints().getGetPc(),pcId)
        .retrieve()
        .bodyToMono(PCDTO.class)
        .timeout(Duration.ofSeconds(5));
  }
}
