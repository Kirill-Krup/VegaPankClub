package com.actisys.adminservice.service.impl;

import com.actisys.adminservice.client.BillingServiceClient;
import com.actisys.adminservice.client.InventoryServiceClient;
import com.actisys.adminservice.client.impl.UserServiceClientImpl;
import com.actisys.adminservice.dto.sessionDtos.AllSessionDTO;
import com.actisys.adminservice.dto.sessionDtos.PCDTO;
import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import com.actisys.adminservice.service.SessionService;
import com.actisys.common.user.UserDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

  private final BillingServiceClient billingServiceClient;
  private final InventoryServiceClient inventoryServiceClient;
  private final UserServiceClientImpl userServiceClient;

  @Override
  public Mono<List<AllSessionDTO>> getAllSession() {
    return billingServiceClient.getAllSessions()
        .flatMapMany(Flux::fromIterable)
        .flatMap(session -> {
          Mono<UserDTO> userMono = userServiceClient.getUserById(session.getUserId());
          Mono<PCDTO> pcMono = inventoryServiceClient.getPcById(session.getPcId())
              .switchIfEmpty(Mono.empty());

          return Mono.zip(userMono, pcMono.switchIfEmpty(Mono.justOrEmpty(null)))
              .map(tuple -> AllSessionDTO.builder()
                  .sessionId(session.getSessionId())
                  .user(tuple.getT1())
                  .tariff(session.getTariff())
                  .startTime(session.getStartTime())
                  .endTime(session.getEndTime())
                  .totalCost(session.getTotalCost())
                  .status(session.getStatus())
                  .pcdto(tuple.getT2())
                  .build());
        })
        .collectList();
  }
}
