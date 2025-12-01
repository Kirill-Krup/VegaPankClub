package com.actisys.adminservice.service;


import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface SessionService {

  Mono<List<SessionDTO>> getAllSession();
}
