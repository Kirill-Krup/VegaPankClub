package com.actisys.apigatewayapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class StaticRoutesConfig {

  @Bean
  public RouterFunction<ServerResponse> indexRouter() {
    return RouterFunctions.route(
        GET("/"),
        request -> ServerResponse.ok()
            .contentType(MediaType.TEXT_HTML)
            .bodyValue(new ClassPathResource("static/html/index.html"))
    );
  }
}
