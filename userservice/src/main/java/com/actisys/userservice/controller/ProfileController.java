package com.actisys.userservice.controller;

import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import com.actisys.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

  private final UserService userService;

  public ProfileController(UserService userService) {
    this.userService = userService;
  }

  /**
   * returns simple profile for header in menu
   */
  @GetMapping("/getProfile")
  public ResponseEntity<UserSimpleProfileDTO> getProfile(
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    if(userId == null || userId.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UserSimpleProfileDTO profileDTO = userService.getProfile(Long.parseLong(userId));
    return ResponseEntity.ok(profileDTO);
  }

  /**
   * returns all profile with info from other services
   * for all rendering of profile (use it for profile page)
   */
  @GetMapping("/getALlProfile")
  public Mono<ResponseEntity<UserAllProfileDTO>> getAllProfile(
      @RequestHeader(value = "X-User-Id", required = false) String userId){
      return userService.getAllProfile(Long.parseLong(userId))
          .map(ResponseEntity::ok)
          .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
  }
}
