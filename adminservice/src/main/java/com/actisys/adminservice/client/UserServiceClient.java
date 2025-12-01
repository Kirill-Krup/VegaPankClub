package com.actisys.adminservice.client;

import com.actisys.common.user.UserDTO;
import reactor.core.publisher.Mono;

public interface UserServiceClient {

  Mono<UserDTO> getUserById(Long userId);
}
