package com.actisys.billingservice.controller;

import com.actisys.billingservice.dto.SessionDtos.SessionResponseDto;
import com.actisys.billingservice.dto.SessionDtos.SessionsInfoDTO;
import com.actisys.billingservice.service.SessionService;
import com.actisys.common.clientDtos.SessionStatsDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {
  private final SessionService sessionService;

  /**
   * This method returns all available pcs
   * for entered date and time.
   * This function needs for render info about
   * available and unavailable pcs
   * */
  @GetMapping("/sessionsForInfo")
  public ResponseEntity<List<SessionsInfoDTO>> getSessionsForInfo(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.plusDays(1).atStartOfDay();
    List<SessionsInfoDTO> allSessions = sessionService.getSessionsInRange(rangeStart, rangeEnd);
    return ResponseEntity.ok(allSessions);
  }

  @GetMapping("/mySessions")
  public ResponseEntity<List<SessionResponseDto>> getMySessions(
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    List<SessionResponseDto> responseDto = sessionService.getUserSessions(Long.parseLong(userId));
    return ResponseEntity.ok(responseDto);
  }

  @PutMapping("/cancelSession/{id}")
  public ResponseEntity<Void> cancelSession(@PathVariable("id") Long id) {
    sessionService.cancelSession(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/stats/{userId}")
  public ResponseEntity<SessionStatsDTO> getSessionStats(@PathVariable("userId") Long userId) {
    SessionStatsDTO statsDTO = sessionService.getUserStats(userId);
    return ResponseEntity.ok(statsDTO);
  }
}
