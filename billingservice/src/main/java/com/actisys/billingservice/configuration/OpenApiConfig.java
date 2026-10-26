package com.actisys.billingservice.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI billingServiceOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("Billing Service API").version("1.0.0"));
  }
}
