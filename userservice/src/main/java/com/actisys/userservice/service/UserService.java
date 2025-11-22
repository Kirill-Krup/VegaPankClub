package com.actisys.userservice.service;

import com.actisys.common.user.UserDTO;
import com.actisys.userservice.dto.UserResponseDtos.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface UserService {

  UserDTO updateUser(Long id, UpdateUserProfileDTO updated);

  UserDTO updateUserPhoto(Long id, String photoPath);

  void deleteUser(Long id);

  UserDTO updateUserCoins(Long id, int coins);

  UserSimpleProfileDTO getProfile(Long userId);

  Mono<UserAllProfileDTO> getAllProfile(Long userId);

  List<UserDTO> getAllUsers();

  UserDTO blockUser(Long id);

  UserDTO unBlockUser(Long id);

  Integer myCoins(Long id);

  UserDTO uploadAndUpdateUserPhoto(Long userId, org.springframework.web.multipart.MultipartFile file);
}