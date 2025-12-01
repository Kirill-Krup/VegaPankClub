package com.actisys.billingservice.service;

import com.actisys.billingservice.dto.SessionDtos.CreateSessionDTO;
import com.actisys.billingservice.dto.SessionDtos.SessionDTO;
import com.actisys.billingservice.dto.SessionDtos.SessionResponseDto;
import com.actisys.billingservice.dto.SessionDtos.SessionsInfoDTO;
import com.actisys.common.clientDtos.SessionStatsDTO;
import com.actisys.common.events.OperationType;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionService {

  SessionStatsDTO getUserStats(Long userId);

  List<SessionsInfoDTO> getSessionsInRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);

  List<SessionResponseDto> getUserSessions(Long userId);

  void cancelSession(Long id);

    SessionDTO createSession(CreateSessionDTO createSessionDTO, String userId);

    void updateStatus(Long orderId, OperationType status);

  List<SessionDTO> getAllSessions();
}
