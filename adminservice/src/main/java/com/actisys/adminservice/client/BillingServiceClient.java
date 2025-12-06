package com.actisys.adminservice.client;

import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface BillingServiceClient {

  Mono<List<SessionDTO>> getAllSessions();

  Mono<SessionDTO> getSessionById(Long orderId);
}
