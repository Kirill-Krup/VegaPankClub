package com.actisys.userservice.service;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.RegisterRequest;
import java.util.List;
import java.util.Optional;

public interface UserService {

  UserDTO createUser(RegisterRequest registerRequest);

  Optional<UserDTO> getUserById(Long id);

  UserDTO getUserByEmail(String email);

  List<UserDTO> getUsersByIds(List<Long> ids);

  UserDTO updateUser(Long id, UserDTO updated);

  UserDTO deleteUser(Long id);

  UserDTO updateUserCoins(Long id, int coins);
}