package com.actisys.authservice.client;

import com.actisys.authservice.dto.RegNewUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(
    name = "user-service",
    url = "${user.service.url}"
)
@Component
public interface UserServiceClient {
  @PostMapping("/api/v1/users/regNewUser")
  Long regNewUser(@RequestBody RegNewUserDTO dto);
}
