package com.actisys.adminservice.config.ServiceProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "productservice")
@Getter
@Setter
public class ProductServiceProperties {
  private String host;
  private Endpoints endpoints = new Endpoints();

  @Getter
  @Setter
  public static class Endpoints {
    private String getAllOrders;
    private String getOrder;
  }
}
