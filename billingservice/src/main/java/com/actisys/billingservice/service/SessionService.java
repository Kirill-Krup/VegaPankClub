package com.actisys.billingservice.service;

import com.actisys.common.dto.clientDtos.SessionStatsDTO;

public interface SessionService {

  SessionStatsDTO getUserStats(Long userId);
}
