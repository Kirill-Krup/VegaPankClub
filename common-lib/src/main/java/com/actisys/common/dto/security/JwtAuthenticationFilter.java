package com.actisys.common.dto.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String userId = request.getHeader("X-User-Id");
    String role = request.getHeader("X-User-Role");

    if (userId != null && role != null) {
      log.debug("Setting security context for user: {} with role: {}", userId, role);

      List<GrantedAuthority> authorities = Collections.singletonList(
          new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
      );

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              Long.parseLong(userId),
              null,
              authorities
          );

      SecurityContextHolder.getContext().setAuthentication(authentication);

      log.debug("Security context set: user={}, role=ROLE_{}", userId, role);
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator") ||
        path.startsWith("/swagger-ui") ||
        path.startsWith("/v3/api-docs");
  }
}
