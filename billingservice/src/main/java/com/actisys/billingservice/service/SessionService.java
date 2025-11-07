package com.actisys.billingservice.service;

import com.actisys.billingservice.dto.SessionDtos.SessionResponseDto;
import com.actisys.billingservice.dto.SessionDtos.SessionsInfoDTO;
import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionService {

  SessionStatsDTO getUserStats(Long userId);

  List<SessionsInfoDTO> getSessionsInRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);

  List<SessionResponseDto> getUserSessions(Long userId);

  void cancelSession(Long id);
}
