package com.actisys.adminservice.service.impl;

import com.actisys.adminservice.client.BillingServiceClient;
import com.actisys.adminservice.client.InventoryServiceClient;
import com.actisys.adminservice.dto.sessionDtos.PCDTO;
import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import com.actisys.adminservice.service.SessionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

  private final BillingServiceClient billingServiceClient;
  private final InventoryServiceClient inventoryServiceClient;

  @Override
  public Mono<List<SessionDTO>> getAllSession() {
    return billingServiceClient.getAllSessions()
        .flatMapMany(Flux::fromIterable)
        .flatMap(session ->
            inventoryServiceClient.getPcById(session.getPcId())
                .map(pc -> enrichSessionWithPcData(session, pc))
        )
        .collectList();
  }

  private SessionDTO enrichSessionWithPcData(SessionDTO session, PCDTO pc) {
    return new SessionDTO(
        session.getSessionId(),
        session.getUserId(),
        session.getPcId(),
        session.getTariff(),
        session.getStartTime(),
        session.getEndTime(),
        session.getTotalCost(),
        session.getStatus(),
        pc
    );
  }
}
