package com.actisys.userservice.controller;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.UserResponseDtos.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import com.actisys.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Map;

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
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserSimpleProfileDTO profileDTO = userService.getProfile(Long.parseLong(userId));
        return ResponseEntity.ok(profileDTO);
    }

    /**
     * returns all profile with info from other services
     * for all rendering of profile (use it for profile page)
     */
    @GetMapping("/getAllProfile")
    public Mono<ResponseEntity<UserAllProfileDTO>> getAllProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        return userService.getAllProfile(Long.parseLong(userId))
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @GetMapping("/myCoins")
    public ResponseEntity<Map<String, Integer>> myCoins(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Integer coins = userService.myCoins(Long.parseLong(userId));
        return ResponseEntity.ok(Map.of("bonusCoins", coins));
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid UpdateUserProfileDTO updateUserProfileDTO) {
        UserDTO dto = userService.updateUser(userId, updateUserProfileDTO);
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ResponseEntity<UserDTO> uploadPhoto(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UserDTO dto = userService.uploadAndUpdateUserPhoto(userId, file);
        return ResponseEntity.ok(dto);
    }
}
