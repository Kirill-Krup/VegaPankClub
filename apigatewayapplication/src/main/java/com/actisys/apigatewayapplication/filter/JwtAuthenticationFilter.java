package com.actisys.apigatewayapplication.filter;

import com.actisys.apigatewayapplication.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final JwtTokenProvider tokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    log.debug("Processing request: {}", path);

    if (isPublicEndpoint(path)) {
      log.debug("Public endpoint, skipping JWT validation: {}", path);
      return chain.filter(exchange);
    }

    if (isStaticResource(path)) {
      log.debug("Static resource, skipping JWT validation: {}", path);
      return chain.filter(exchange);
    }

    String token = extractTokenFromCookie(exchange);

    if (token == null) {
      log.warn("Missing JWT token for protected endpoint: {}", path);
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    try {
      Jws<Claims> jws = tokenProvider.verify(token);
      Claims claims = jws.getPayload();

      String userId = claims.getSubject();
      String username = extractUsername(claims);
      List<String> roles = extractRoles(claims);
      String primaryRole = roles.isEmpty() ? "USER" : roles.get(0);
      log.info("Authenticated user: {} (ID: {}) with roles: {}", username, userId, roles);

      ServerHttpRequest mutatedRequest = request.mutate()
          .header("X-User-Id", userId)
          .header("X-Username", username)
          .header("X-User-Roles", String.join(",", roles))
          .header("Authorization", "Bearer " + token)
          .build();

      return chain.filter(exchange.mutate().request(mutatedRequest).build());

    } catch (JwtException ex) {
      log.warn("JWT validation failed for path {}: {}", path, ex.getMessage());
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    } catch (Exception ex) {
      log.error("Error during JWT authentication for path {}: {}", path, ex.getMessage(), ex);
      exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
      return exchange.getResponse().setComplete();
    }
  }

  private String extractTokenFromCookie(ServerWebExchange exchange) {
    HttpCookie cookie = exchange.getRequest().getCookies().getFirst("auth_token");
    if (cookie != null) {
      log.debug("Found auth_token cookie");
      return cookie.getValue();
    }
    log.debug("No auth_token cookie found");
    return null;
  }

  private String extractUsername(Claims claims) {
    String username = claims.get("username", String.class);
    if (username == null) {
      username = claims.get("uname", String.class);
    }
    if (username == null) {
      username = claims.getSubject();
    }
    return username;
  }

  @SuppressWarnings("unchecked")
  private List<String> extractRoles(Claims claims) {
    List<String> roles = claims.get("roles", List.class);
    return roles != null ? roles : List.of();
  }

  private boolean isPublicEndpoint(String path) {
    return path.startsWith("/api/v1/auth/login") ||
        path.startsWith("/api/v1/auth/register") ||
        path.startsWith("/api/v1/tariffs/allTariffs");
  }

  private boolean isStaticResource(String path) {
    return path.startsWith("/static/") ||
        path.startsWith("/html/") ||
        path.startsWith("/css/") ||
        path.startsWith("/js/") ||
        path.equals("/");
  }

  @Override
  public int getOrder() {
    return -100;
  }
}