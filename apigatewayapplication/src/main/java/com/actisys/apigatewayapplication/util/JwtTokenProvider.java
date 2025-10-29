package com.actisys.apigatewayapplication.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.ttl-seconds:860000}")
  private long ttlSeconds;

  @Value("${jwt.issuer:actisys-auth}")
  private String issuer;

  private SecretKey key;
  private Duration ttl;

  @PostConstruct
  void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.ttl = Duration.ofSeconds(ttlSeconds);
  }

  public String generate(String userId, String username, List<String> roles, Map<String, Object> extra) {
    Instant now = Instant.now();
    JwtBuilder b = Jwts.builder()
        .setSubject(userId)
        .claim("uname", username)
        .claim("roles", roles)
        .setIssuer(issuer)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(ttl)))
        .signWith(key, SignatureAlgorithm.HS256);
    if (extra != null) extra.forEach(b::claim);
    return b.compact();
  }

  public Jws<Claims> verify(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token);
  }
}
