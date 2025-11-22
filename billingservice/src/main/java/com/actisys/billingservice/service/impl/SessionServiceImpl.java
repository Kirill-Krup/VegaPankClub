package com.actisys.billingservice.service.impl;


import com.actisys.billingservice.client.InventoryServiceClient;
import com.actisys.billingservice.dto.SessionDtos.CreateSessionDTO;
import com.actisys.billingservice.dto.SessionDtos.SessionDTO;
import com.actisys.billingservice.dto.SessionDtos.SessionResponseDto;
import com.actisys.billingservice.dto.SessionDtos.SessionsInfoDTO;
import com.actisys.billingservice.exception.SessionNotFoundException;
import com.actisys.billingservice.exception.TariffNotFoundException;
import com.actisys.billingservice.mapper.SessionMapper;
import com.actisys.billingservice.mapper.TariffMapper;
import com.actisys.billingservice.model.Session;
import com.actisys.billingservice.model.SessionStatus;
import com.actisys.billingservice.model.Tariff;
import com.actisys.billingservice.repository.SessionRepository;
import com.actisys.billingservice.repository.TariffRepository;
import com.actisys.billingservice.service.SessionService;
import com.actisys.common.clientDtos.PcResponseDTO;
import com.actisys.common.clientDtos.SessionStatsDTO;
import com.actisys.common.events.order.CreateOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {


  private final SessionRepository sessionRepository;
  private final SessionMapper sessionMapper;
  private final InventoryServiceClient inventoryServiceClient;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final TariffMapper tariffMapper;
  private final TariffRepository tariffRepository;

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

  @Override
  public SessionDTO createSession(CreateSessionDTO createSessionDTO, String userId) {

    Tariff tariff = tariffRepository.findById(createSessionDTO.getTariffId()).orElseThrow(() ->
            new TariffNotFoundException(createSessionDTO.getTariffId()));

      long hours = Duration.between(createSessionDTO.getStartTime(), createSessionDTO.getEndTime()).toHours();
      BigDecimal pricePerHour = tariff.getPrice().divide(BigDecimal.valueOf(tariff.getHours()), 2, RoundingMode.HALF_UP);
      BigDecimal totalCost = pricePerHour.multiply(BigDecimal.valueOf(hours));

    Session session = Session.builder()
            .pcId(createSessionDTO.getPcId())
            .userId(Long.valueOf(userId))
            .tariff(tariff).
            totalCost(totalCost)
            .startTime(createSessionDTO.getStartTime())
            .endTime(createSessionDTO.getEndTime())
            .status(SessionStatus.PENDING)
            .build();

    Session savedSession = sessionRepository.save(session);

    CreateOrderEvent createOrderEvent = new CreateOrderEvent();
    createOrderEvent.setOrderId(savedSession.getSessionId());
    createOrderEvent.setUserId(savedSession.getUserId());
    createOrderEvent.setAmount(savedSession.getTotalCost());

    kafkaTemplate.send("CREATE_BOOKING", createOrderEvent);

    return sessionMapper.toDTO(savedSession);
  }

  @Override
  public void updateStatus(Long orderId, String status) {
    Session session = sessionRepository.findById(orderId).orElseThrow(()->new RuntimeException("Order not found"));

    if (status.equals("FAILED")) {
      session.setStatus(SessionStatus.CANCELLED);
    }
    else if(status.equals("SUCCEEDED")) {
      session.setStatus(SessionStatus.PAID);
    }
  }
}
