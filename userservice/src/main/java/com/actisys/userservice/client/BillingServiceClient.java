package com.actisys.userservice.client;

import com.actisys.common.clientDtos.SessionStatsDTO;
import reactor.core.publisher.Mono;

public interface BillingServiceClient {
  Mono<SessionStatsDTO> getUserSessionStats(Long userId);
}
