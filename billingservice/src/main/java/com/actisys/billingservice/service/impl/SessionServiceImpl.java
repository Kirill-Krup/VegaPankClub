package com.actisys.billingservice.service.impl;


import com.actisys.billingservice.client.InventoryServiceClient;
import com.actisys.billingservice.dto.SessionDtos.CreateSessionDTO;
import com.actisys.billingservice.dto.SessionDtos.SessionDTO;
import com.actisys.billingservice.dto.SessionDtos.SessionResponseDto;
import com.actisys.billingservice.dto.SessionDtos.SessionsInfoDTO;
import com.actisys.billingservice.exception.OrderNotFoundException;
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
import com.actisys.common.events.OperationType;
import com.actisys.common.events.order.CreateOrderEvent;
import com.actisys.common.events.user.RefundMoneyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    updateSessionsStatuses(sessions);
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

    updateSessionsStatuses(allSessionForRange);

    return allSessionForRange.stream().map(sessionMapper::toInfoDTO).collect(Collectors.toList());
  }

  @Override
  public List<SessionResponseDto> getUserSessions(Long userId) {
    List<Session> sessions = sessionRepository.findAllByUserId(userId);

    updateSessionsStatuses(sessions);

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
    session = updateSessionStatusBasedOnTime(session);
    if (session.getStatus() == SessionStatus.IN_PROGRESS) {
      throw new IllegalStateException("Cannot cancel session that is in progress");
    }
    if (session.getStatus() == SessionStatus.COMPLETED) {
      throw new IllegalStateException("Cannot cancel completed session");
    }

    session.setStatus(SessionStatus.CANCELLED);
    sessionRepository.save(session);

    RefundMoneyEvent refundMoneyEvent = new RefundMoneyEvent();
    refundMoneyEvent.setUserId(session.getUserId());
    refundMoneyEvent.setPaymentId(session.getPaymentId());
    refundMoneyEvent.setAmount(session.getTotalCost());
    kafkaTemplate.send("REFUND_MONEYS_EVENT", refundMoneyEvent);
  }

  @Override
  public SessionDTO createSession(CreateSessionDTO createSessionDTO, String userId) {
    LocalDateTime now = LocalDateTime.now();
    if (createSessionDTO.getStartTime().isBefore(now)) {
      throw new IllegalArgumentException("Session start time cannot be in the past");
    }

    if (createSessionDTO.getEndTime().isBefore(createSessionDTO.getStartTime())) {
      throw new IllegalArgumentException("Session end time cannot be before start time");
    }

    Tariff tariff = tariffRepository.findById(createSessionDTO.getTariffId()).orElseThrow(() ->
        new TariffNotFoundException(createSessionDTO.getTariffId()));

    long minutes = Duration
        .between(createSessionDTO.getStartTime(), createSessionDTO.getEndTime()).toMinutes();
    BigDecimal hours = BigDecimal.valueOf(minutes)
        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    BigDecimal pricePerHour = tariff.getPrice()
        .divide(BigDecimal.valueOf(tariff.getHours()), 2, RoundingMode.HALF_UP);

    BigDecimal totalCost = pricePerHour.multiply(hours).setScale(2, RoundingMode.HALF_UP);

    Session session = Session.builder()
        .pcId(createSessionDTO.getPcId())
        .userId(Long.valueOf(userId))
        .tariff(tariff)
        .totalCost(totalCost)
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
  @Transactional
  public void updateStatus(Long orderId, OperationType status) {
    Session session = sessionRepository.findById(orderId).orElseThrow(()->
        new OrderNotFoundException(orderId));

    if (status == OperationType.ERROR) {
      session.setStatus(SessionStatus.ERROR);
    } else {
      session.setStatus(SessionStatus.PAID);
    }

    sessionRepository.save(session);

    if (status == OperationType.SUCCESS) {
      updateSessionStatusBasedOnTime(session);
    }
  }

  @Override
  public List<SessionDTO> getAllSessions() {
    List<Session> sessions = sessionRepository.findAll();
    updateSessionsStatuses(sessions);
    return sessions.stream().map(sessionMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  public SessionDTO getSessionById(Long id) {
    Session session = sessionRepository.findById(id)
        .orElseThrow(() -> new SessionNotFoundException(id));
    log.info(session.toString() + " with id {}", id);
    session = updateSessionStatusBasedOnTime(session);
    log.info(session.toString());
    return sessionMapper.toDTO(session);
  }

  private Session updateSessionStatusBasedOnTime(Session session){
    LocalDateTime now = LocalDateTime.now();
    if(now.isAfter(session.getStartTime()) && now.isBefore(session.getEndTime())){
      session.setStatus(SessionStatus.IN_PROGRESS);
    }else if(now.isAfter(session.getEndTime())){
      session.setStatus(SessionStatus.COMPLETED);
    } else {
      return session;
    }
    return sessionRepository.save(session);
  }

  private void updateSessionsStatuses(List<Session> sessions) {
    LocalDateTime now = LocalDateTime.now();
    List<Session> sessionsToUpdate = new ArrayList<>();

    for (Session session : sessions) {
      if (session.getStatus() == SessionStatus.COMPLETED ||
          session.getStatus() == SessionStatus.CANCELLED ||
          session.getStatus() == SessionStatus.ERROR ||
          session.getStatus() == SessionStatus.REFUNDED ||
          session.getStatus() == SessionStatus.NOT_SHOW) {
        continue;
      }

      SessionStatus newStatus = null;

      if(now.isAfter(session.getStartTime()) && now.isBefore(session.getEndTime())){
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
          newStatus = SessionStatus.IN_PROGRESS;
        }
      } else if(now.isAfter(session.getEndTime())){
        if (session.getStatus() != SessionStatus.COMPLETED) {
          newStatus = SessionStatus.COMPLETED;
        }
      }

      if (newStatus != null) {
        log.info("Session {} status changed from {} to {}",
            session.getSessionId(), session.getStatus(), newStatus);
        session.setStatus(newStatus);
        sessionsToUpdate.add(session);
      }
    }

    if (!sessionsToUpdate.isEmpty()) {
      sessionRepository.saveAll(sessionsToUpdate);
      log.info("Saved {} updated sessions to database", sessionsToUpdate.size());
    }
  }
}
