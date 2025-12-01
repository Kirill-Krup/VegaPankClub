package com.actisys.adminservice.client.impl;

import com.actisys.adminservice.client.ProductServiceClient;
import com.actisys.adminservice.config.ProductServiceProperties;
import com.actisys.adminservice.dto.orderDtos.OrderDTO;
import java.time.Duration;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceClientImpl implements ProductServiceClient {

  private final WebClient webClient;
  private final ProductServiceProperties properties;

  public ProductServiceClientImpl(
      ProductServiceProperties properties,
      WebClient.Builder webClientBuilder) {
    this.properties = properties;
    this.webClient = webClientBuilder
        .baseUrl(properties.getHost())
        .build();
  }

  @Override
  public Mono<List<OrderDTO>> getAllOrders() {
    return webClient.get()
        .uri(properties.getEndpoints().getGetAllOrders())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<OrderDTO>>() {})
        .timeout(Duration.ofSeconds(5));
  }
}
