package com.actisys.adminservice.config.ServiceProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "billingservice")
@Getter
@Setter
public class BillingServiceProperties{
  private String host;
  private Endpoints endpoints = new Endpoints();

  @Getter
  @Setter
  public static class Endpoints {
    private String getAllSessions;
    private String getSession;
  }
}
