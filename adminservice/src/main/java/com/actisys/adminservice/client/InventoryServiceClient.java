package com.actisys.adminservice.client;

import com.actisys.adminservice.dto.sessionDtos.PCDTO;
import reactor.core.publisher.Mono;

public interface InventoryServiceClient {

  Mono<PCDTO> getPcById(Long pcId);
}
