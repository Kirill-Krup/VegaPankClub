package com.actisys.billingservice.controller;

import com.actisys.billingservice.service.SessionService;
import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {
  private final SessionService sessionService;

  @GetMapping("/stats/{userId}")
  public ResponseEntity<SessionStatsDTO> getSessionStats(@PathVariable("userId") Long userId) {
    SessionStatsDTO statsDTO = sessionService.getUserStats(userId);
    return ResponseEntity.ok(statsDTO);
  }
}
