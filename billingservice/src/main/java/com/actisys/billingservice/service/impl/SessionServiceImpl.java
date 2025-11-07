package com.actisys.billingservice.service.impl;

import com.actisys.billingservice.client.InventoryServiceClient;
import com.actisys.billingservice.dto.SessionDtos.SessionResponseDto;
import com.actisys.billingservice.dto.SessionDtos.SessionsInfoDTO;
import com.actisys.billingservice.exception.SessionNotFoundException;
import com.actisys.billingservice.mapper.SessionMapper;
import com.actisys.billingservice.mapper.TariffMapper;
import com.actisys.billingservice.model.Session;
import com.actisys.billingservice.model.SessionStatus;
import com.actisys.billingservice.repository.SessionRepository;
import com.actisys.billingservice.service.SessionService;
import com.actisys.common.dto.clientDtos.PcResponseDTO;
import com.actisys.common.dto.clientDtos.SessionStatsDTO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

  private final SessionRepository sessionRepository;
  private final SessionMapper sessionMapper;
  private final InventoryServiceClient inventoryServiceClient;
  private final TariffMapper tariffMapper;

  @Override
  public SessionStatsDTO getUserStats(Long userId) {
    List<Session> sessions = sessionRepository.findAllByUserIdAndEndTimeIsNotNull(userId);

    int totalSessions = sessions.size();

    double totalHours = sessions.stream()
        .mapToDouble(s -> Duration.between(s.getStartTime(), s.getEndTime())
            .toMinutes() / 60.0)
        .sum();
    return new SessionStatsDTO(totalSessions, totalHours);
  }

  @Override
  public List<SessionsInfoDTO> getSessionsInRange(LocalDateTime rangeStart,
      LocalDateTime rangeEnd) {
    List<Session> allSessionForRange = sessionRepository.findSessionsIntersectingDay(rangeStart, rangeEnd);
    return allSessionForRange.stream().map(sessionMapper::toInfoDTO).collect(Collectors.toList());
  }

  @Override
  public List<SessionResponseDto> getUserSessions(Long userId) {
    List<Session> sessions = sessionRepository.findAllByUserId(userId);
    if(sessions.isEmpty()) {
      log.debug("No sessions found for user: {}", userId);
      return List.of();
    }
    Set<Long> pcIds = sessions.stream().map(Session::getPcId).collect(Collectors.toSet());
    List<PcResponseDTO> pcs = inventoryServiceClient.getPcInfoByIds(new ArrayList<>(pcIds));
    Map<Long, PcResponseDTO> pcMap = pcs.stream().collect(Collectors.toMap(PcResponseDTO::getId,
        pc -> pc));
    return sessions.stream()
        .map(session -> SessionResponseDto.builder()
            .sessionId(session.getSessionId())
            .pc(pcMap.get(session.getPcId()))
            .tariff(tariffMapper.toDTO(session.getTariff()))
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .totalCost(session.getTotalCost())
            .status(session.getStatus())
            .build())
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void cancelSession(Long id) {
    Session session = sessionRepository.findById(id).orElseThrow(()->
        new SessionNotFoundException(id));
    session.setStatus(SessionStatus.CANCELLED);
    sessionRepository.save(session);

    // THIS METHOD NEED TO SEND EVENT TO KAFKA FOR REFUNDED MONEYS FOR USER
  }
}
