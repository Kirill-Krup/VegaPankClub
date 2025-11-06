package com.actisys.billingservice.service;

import com.actisys.billingservice.dto.SessionsInfoDTO;
import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface SessionService {

  SessionStatsDTO getUserStats(Long userId);

  List<SessionsInfoDTO> getSessionsInRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);
}
