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

  /**
   * Calculates user gaming statistics from completed sessions.
   * Returns total sessions count and total gaming hours.
   *
   * @param userId user identifier
   * @return session statistics DTO with count and hours
   */
  SessionStatsDTO getUserStats(Long userId);

  /**
   * Retrieves sessions overlapping specified date range for reporting.
   *
   * @param rangeStart start of date range
   * @param rangeEnd end of date range
   * @return list of sessions intersecting the range
   */
  List<SessionsInfoDTO> getSessionsInRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);

  /**
   * Gets user sessions enriched with PC details from inventory service.
   * Includes tariff, times, cost and status for each session.
   *
   * @param userId user identifier
   * @return list of user sessions with PC information
   */
  List<SessionResponseDto> getUserSessions(Long userId);

  /**
   * Cancels session and triggers money refund via Kafka event.
   * Sets session status to CANCELLED and publishes RefundMoneyEvent.
   *
   * @param id session identifier to cancel
   */
  void cancelSession(Long id);

  /**
   * Creates new gaming session with cost calculation based on tariff and duration.
   * Calculates total cost using tariff hourly rate, publishes CreateOrderEvent.
   *
   * @param createSessionDTO session data with PC, tariff and time range
   * @param userId string representation of user ID
   * @return created session DTO
   */
  SessionDTO createSession(CreateSessionDTO createSessionDTO, String userId);

  /**
   * Updates session status based on payment operation result.
   * Sets PAID for SUCCESS, ERROR for failed payments.
   *
   * @param orderId session identifier
   * @param status payment operation result
   */
  void updateStatus(Long orderId, OperationType status);

  /**
   * Retrieves all sessions for administrative purposes.
   *
   * @return list of all sessions as DTOs
   */
  List<SessionDTO> getAllSessions();

  /**
   * Returns session by sessionId
   *
   * @param id session identifier
   */
  SessionDTO getSessionById(Long id);
}
