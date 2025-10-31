package com.actisys.userservice.service;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.UserResponseDtos.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import java.util.List;
import java.util.Optional;
import reactor.core.publisher.Mono;

public interface UserService {

  Optional<UserDTO> getUserById(Long id);

  UserDTO getUserByEmail(String email);

  List<UserDTO> getUsersByIds(List<Long> ids);

  UserDTO updateUser(Long id, UpdateUserProfileDTO updated);

  void updateUserPhoto(Long id, String photoPath);

  void deleteUser(Long id);

  UserDTO updateUserCoins(Long id, int coins);

  UserSimpleProfileDTO getProfile(Long userId);

  Mono<UserAllProfileDTO> getAllProfile(Long userId);
}