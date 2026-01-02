package com.actisys.adminservice.controller;

import com.actisys.adminservice.dto.sessionDtos.AllSessionDTO;
import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import com.actisys.adminservice.service.SessionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/sessions")
public class SessionController {
  private final SessionService sessionService;

  @GetMapping("/getAllSessions")
  public Mono<ResponseEntity<List<AllSessionDTO>>> getAllSessions(){
    return sessionService.getAllSession()
        .map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }
}
