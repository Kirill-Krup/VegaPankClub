package com.example.vegapank.Service;

import com.example.vegapank.DTO.UserDTO;
import com.example.vegapank.Exception.UserNotFoundException;
import com.example.vegapank.Mapper.UserMapper;
import com.example.vegapank.Repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
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

  @Cacheable(value = "user", key = "#result.email")
  public UserDTO createUser(UserDTO userDTO) {
    User entity = userMapper.toEntity(userDTO);
    User savedEntity = userRepository.save(entity);
    return userMapper.toDTO(savedEntity);
  }

  @Cacheable(value = "user", key = "#email")
  public UserDTO getUserByEmail(String email) {
    User user = userRepository.findByEmail(email);
    if(user == null) {
      throw new UserNotFoundException(email);
    }
    return userMapper.toDTO(user);
  }

  @Cacheable(value = "user", key = "#id")
  public UserDTO getUserById(Long id) {
    User user = userRepository.findUserById(id);
    if(user == null) {
      throw new UserNotFoundException(id);
    }
    return userMapper.toDTO(userRepository.findUserById(id));
  }
}
