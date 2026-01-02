package com.actisys.adminservice.client.impl;

import com.actisys.adminservice.client.UserServiceClient;
import com.actisys.adminservice.config.ServiceProperties.UserServiceProperties;
import com.actisys.common.user.UserDTO;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserServiceClientImpl implements UserServiceClient {

  private final WebClient webClient;
  private final UserServiceProperties properties;

  public UserServiceClientImpl(
      UserServiceProperties properties,
      WebClient.Builder webClientBuilder) {
    this.properties = properties;
    this.webClient = webClientBuilder
        .baseUrl(properties.getHost())
        .build();
  }


  @Override
  public Mono<UserDTO> getUserById(Long userId) {
    return webClient.get()
        .uri(properties.getEndpoints().getGetUser(), userId)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .timeout(Duration.ofSeconds(5));
  }
}
