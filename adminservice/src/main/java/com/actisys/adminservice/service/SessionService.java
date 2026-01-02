package com.actisys.adminservice.service;

import com.actisys.adminservice.dto.sessionDtos.AllSessionDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface SessionService {

  Mono<List<AllSessionDTO>> getAllSession();
}
