package com.actisys.apigatewayapplication.filter;

import com.actisys.apigatewayapplication.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);
  private final JwtTokenProvider tokenProvider;

  public JwtTokenFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = null;

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
    }

    if (token != null) {
      try {
        Jws<Claims> jws = tokenProvider.verify(token);
        Claims claims = jws.getPayload();

        String username = extractUsername(claims);
        List<String> roles = extractRoles(claims);

        if (username != null) {
          request.setAttribute("userId", claims.getSubject());
          request.setAttribute("username", username);
          request.setAttribute("roles", roles);

          log.debug("Authenticated user: {} with roles: {}", username, roles);
        }

      } catch (JwtException ex) {
        log.warn("JWT validation failed: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid JWT token");
        return;
      } catch (Exception ex) {
        log.error("Error during JWT authentication: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("Authentication error");
        return;
      }
    } else {
      if (!isPublicEndpoint(request)) {
        log.warn("Missing JWT token for protected endpoint: {}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Missing authentication token");
        return;
      }
    }

    filterChain.doFilter(request, response);
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

  private boolean isPublicEndpoint(HttpServletRequest request) {
    String path = request.getServletPath();
    return path.startsWith("/api/v1/auth/login") ||
        path.startsWith("/api/v1/auth/register");
  }
}