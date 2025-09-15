package com.example.vegapank.Service;

import com.example.vegapank.DTO.UserDTO;
import com.example.vegapank.Mapper.UserMapper;
import com.example.vegapank.Repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private UserRepository userRepository;

  private UserMapper userMapper;

  public UserService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  public UserDTO createUser(UserDTO userDTO) {
    User entity = userMapper.toEntity(userDTO);
    User savedEntity = userRepository.save(entity);
    return userMapper.toDTO(savedEntity);
  }
}
