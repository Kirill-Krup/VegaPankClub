package com.actisys.userservice.service.impl;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.UserService;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService{
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Override
  public UserDTO createUser(RegisterRequest registerRequest) {
    User entity = userMapper.fromRegisterRequest(registerRequest);

    entity.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
    entity.setBonusCoins(0);
    entity.setWallet(0.0);
    User savedEntity = userRepository.save(entity);
    UserDTO result = userMapper.toDTO(savedEntity);

    System.out.println("Created user with ID: " + result.getId());
    return result;
  }

  @Cacheable(value = "users", key = "#id")
  public Optional<UserDTO> getUserById(Long id) {
    return Optional.ofNullable(userRepository.findUserById(id)).map(userMapper::toDTO);
  }

  @Override
  @Cacheable(value = "users", key = "#email")
  public UserDTO getUserByEmail(String email) {
    User user = userRepository.findUserByEmail(email);
    if(user == null) {
      throw new UserNotFoundException(email);
    }
    return userMapper.toDTO(user);
  }

  @Override
  public List<UserDTO> getUsersByIds(List<Long> ids) {
    return ids.stream().map(this::getUserById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Caching(put = {
      @CachePut(value = "users", key = "#id"),
      @CachePut(value = "users",key = "#result.email", condition = "#result != null ")
  })
  public UserDTO updateUser(Long id, UserDTO updated) {
    User updatedEntity = userRepository.findById(id).map(user -> {
      user.setFullName(updated.getFullName());
      user.setEmail(updated.getEmail());
      user.setPhone(updated.getPhone());
      user.setPhotoPath(updated.getPhotoPath());
      return user;
    }).orElseThrow(() -> new UserNotFoundException(id));
    User savedEntity = userRepository.save(updatedEntity);
    return userMapper.toDTO(savedEntity);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "users", key = "#id"),
      @CacheEvict(value = "users", key = "#result.email")
  })
  public UserDTO deleteUser(Long id) {
    if(!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    UserDTO userDTO = userMapper.toDTO(user);
    userRepository.deleteById(id);
    return userDTO;
  }

  @Override
  public UserDTO updateUserCoins(Long id, int coins) {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    user.setBonusCoins(user.getBonusCoins() + coins);
    userRepository.save(user);
    return userMapper.toDTO(user);
  }


}