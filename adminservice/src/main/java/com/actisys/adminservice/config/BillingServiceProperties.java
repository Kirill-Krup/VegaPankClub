package com.actisys.adminservice.config;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "billingservice")
@Getter
public class BillingServiceProperties{
  private String host;
  private Endpoints endpoints = new Endpoints();

  @Getter
  public static class Endpoints {
    private String getAllSessions;
  }
}
