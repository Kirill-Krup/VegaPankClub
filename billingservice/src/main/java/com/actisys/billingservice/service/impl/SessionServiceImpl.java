package com.actisys.billingservice.service.impl;

import com.actisys.billingservice.dto.SessionsInfoDTO;
import com.actisys.billingservice.mapper.SessionMapper;
import com.actisys.billingservice.model.Session;
import com.actisys.billingservice.repository.SessionRepository;
import com.actisys.billingservice.service.SessionService;
import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

  private final SessionRepository sessionRepository;
  private final SessionMapper sessionMapper;

  @Override
  public SessionStatsDTO getUserStats(Long userId) {
    List<Session> sessions = sessionRepository.findAllByUserIdAndEndTimeIsNotNull(userId);

    int totalSessions = sessions.size();

    double totalHours = sessions.stream()
        .mapToDouble(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes() / 60.0)
        .sum();
    return new SessionStatsDTO(totalSessions, totalHours);
  }

  @Override
  public List<SessionsInfoDTO> getSessionsInRange(LocalDateTime rangeStart,
      LocalDateTime rangeEnd) {
    List<Session> allSessionForRange = sessionRepository.findSessionsIntersectingDay(rangeStart, rangeEnd);
    return allSessionForRange.stream().map(sessionMapper::toInfoDTO).collect(Collectors.toList());
  }
}
