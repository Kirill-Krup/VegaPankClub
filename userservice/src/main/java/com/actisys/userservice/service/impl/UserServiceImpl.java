package com.actisys.userservice.service.impl;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.client.BillingServiceClient;
import com.actisys.userservice.dto.UserResponseDtos.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final BillingServiceClient billingServiceClient;

  public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
      BillingServiceClient billingServiceClient) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.billingServiceClient = billingServiceClient;
  }

  @Cacheable(value = "users", key = "#id")
  public Optional<UserDTO> getUserById(Long id) {
    return Optional.ofNullable(userRepository.findUserById(id))
        .map(userMapper::toDTO);
  }

  @Override
  @Cacheable(value = "users", key = "#email")
  public UserDTO getUserByEmail(String email) {
    User user = userRepository.findUserByEmail(email);
    if (user == null) {
      throw new UserNotFoundException(email);
    }
    return userMapper.toDTO(user);
  }

  @Override
  public List<UserDTO> getUsersByIds(List<Long> ids) {
    return ids.stream()
        .map(this::getUserById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Caching(
      put = {
          @CachePut(value = "users", key = "#id"),
          @CachePut(value = "users", key = "#result.email", condition = "#result != null")
      },
      evict = {
          @CacheEvict(value = "userSimpleProfiles", key = "#id"),
          @CacheEvict(value = "userAllProfiles", key = "#id")
      }
  )
  public UserDTO updateUser(Long id, UpdateUserProfileDTO updated) {
    log.debug("Updating user profile: {}", id);

    User updatedEntity = userRepository.findById(id)
        .map(user -> {
          user.setFullName(updated.getFullName());
          user.setEmail(updated.getEmail());
          user.setPhone(updated.getPhone());
          return user;
        })
        .orElseThrow(() -> new UserNotFoundException(id));

    User savedEntity = userRepository.save(updatedEntity);
    return userMapper.toDTO(savedEntity);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "users", key = "#id"),
      @CacheEvict(value = "userSimpleProfiles", key = "#id"),
      @CacheEvict(value = "userAllProfiles", key = "#id")
  })
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    userRepository.deleteById(id);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "users", key = "#id"),
      @CacheEvict(value = "userSimpleProfiles", key = "#id"),
      @CacheEvict(value = "userAllProfiles", key = "#id")
  })
  public UserDTO updateUserCoins(Long id, int coins) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    user.setBonusCoins(user.getBonusCoins() + coins);
    User saved = userRepository.save(user);
    return userMapper.toDTO(saved);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "users", key = "#userId"),
      @CacheEvict(value = "userSimpleProfiles", key = "#userId"),
      @CacheEvict(value = "userAllProfiles", key = "#userId")
  })
  public void updateUserPhoto(Long userId, String photoPath) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    user.setPhotoPath(photoPath);
    userRepository.save(user);
  }

  @Override
  @Cacheable(value = "userSimpleProfiles", key = "#userId")
  public UserSimpleProfileDTO getProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    return UserSimpleProfileDTO.builder()
        .login(user.getLogin())
        .wallet(user.getWallet())
        .photoPath(user.getPhotoPath())
        .isBanned(user.isBanned())
        .role(user.getRole())
        .build();
  }

  @Override
  public Mono<UserAllProfileDTO> getAllProfile(Long userId) {
    return Mono.fromSupplier(() -> {
          User user = userRepository.findById(userId)
              .orElseThrow(() -> new UserNotFoundException(userId));
          return userMapper.toAllProfileDTO(user);
        })
        .flatMap(dto ->
            billingServiceClient.getUserSessionStats(userId)
                .map(sessionStats -> {
                  dto.setSessionStats(sessionStats);
                  return dto;
                })
                .doOnError(error ->
                    log.warn("Failed to fetch session stats for user {}: {}", userId, error.getMessage())
                )
                .onErrorReturn(dto)
        )
        .cache();
  }

  @Override
  public List<UserDTO> getAllUsers() {
    List<User> users = userRepository.findAllUsers();
    return users.stream().map(userMapper::toDTO).collect(Collectors.toList());
  }
}
