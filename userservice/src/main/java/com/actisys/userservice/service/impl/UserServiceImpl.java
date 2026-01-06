package com.actisys.userservice.service.impl;

import com.actisys.common.events.OperationType;
import com.actisys.common.events.PaymentType;
import com.actisys.common.events.user.CreateWalletEvent;
import com.actisys.common.events.user.RefundMoneyEvent;
import com.actisys.common.events.user.WithdrawEvent;
import com.actisys.common.user.UserDTO;
import com.actisys.userservice.client.BillingServiceClient;
import com.actisys.userservice.dto.UserResponseDtos.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import com.actisys.userservice.exception.InsufficientFundsException;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.UserPhotoStorageService;
import com.actisys.userservice.service.UserService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final BillingServiceClient billingServiceClient;
  private final UserPhotoStorageService userPhotoStorageService;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
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
  @CacheEvict(value = "allUsers", allEntries = true)
  public void deleteUser(Long id) {
    log.debug("Deleting user: {}", id);
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    userRepository.deleteById(id);
  }

  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
  public UserDTO updateUserCoins(Long id, int coins) {
    log.debug("Updating coins for user {}: adding {} coins", id, coins);
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    int oldBalance = user.getBonusCoins();
    user.setBonusCoins(oldBalance + coins);
    User saved = userRepository.save(user);

    log.debug("User {} coins updated: {} -> {}", id, oldBalance, saved.getBonusCoins());
    return userMapper.toDTO(saved);
  }

  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
  public UserDTO updateUserPhoto(Long userId, String photoPath) {
    log.debug("Updating photo for user {}: {}", userId, photoPath);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    user.setPhotoPath(photoPath);
    User saved = userRepository.save(user);
    return userMapper.toDTO(saved);
  }

  @Override
  public UserSimpleProfileDTO getProfile(Long userId) {
    log.debug("Fetching simple profile from database: {}", userId);
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
    log.debug("Fetching full profile for user: {}", userId);
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
        .cache(Duration.ofMinutes(10));
  }

  @Override
  @Cacheable(value = "allUsers")
  public List<UserDTO> getAllUsers() {
    log.debug("Fetching all users from database");
    List<User> users = userRepository.findAllUsers();
    return users.stream()
        .map(userMapper::toDTO)
        .collect(Collectors.toList());
  }


  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
  public UserDTO blockUser(Long id) {
    log.debug("Blocking user: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    if (user.getRole() == 2) {
      throw new RuntimeException("You can't block admin");
    }

    user.setBanned(true);
    User saved = userRepository.save(user);
    UserDTO result = userMapper.toDTO(saved);

    log.debug("User blocked successfully: {}, isBanned={}", id, saved.isBanned());
    return result;
  }


  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
  public UserDTO unBlockUser(Long id) {
    log.debug("Unblocking user: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    user.setBanned(false);
    User saved = userRepository.save(user);
    UserDTO result = userMapper.toDTO(saved);

    log.debug("User unblocked successfully: {}, isBanned={}", id, saved.isBanned());
    return result;
  }

  @Override
  public Integer myCoins(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    return user.getBonusCoins();
  }

  @Override
  @Transactional
  @CacheEvict(value = "allUsers", allEntries = true)
  public UserDTO uploadAndUpdateUserPhoto(Long userId, MultipartFile file) {
    log.debug("Uploading new photo for user {}", userId);

    String key = userPhotoStorageService.uploadUserPhoto(userId, file);

    return updateUserPhoto(userId, key);
  }

  @Override
  public void withdrawMoney(CreateWalletEvent event) {
    User user = userRepository.findById(event.getUserId())
            .orElseThrow(() -> new UserNotFoundException(event.getUserId()));

    WithdrawEvent withdrawEvent = new WithdrawEvent();
    withdrawEvent.setPaymentId(event.getPaymentId());

    if (user.getWallet().compareTo(event.getCost()) < 0) {
      withdrawEvent.setStatus(OperationType.ERROR);
      kafkaTemplate.send("WALLET_EVENT", withdrawEvent);
      throw new InsufficientFundsException(event.getUserId());
    }

    user.setWallet(user.getWallet().subtract(event.getCost()));

    if (event.getPaymentType().equals(PaymentType.BOOKING)) {
      user.setBonusCoins(user.getBonusCoins() + event.getCost().multiply(BigDecimal.valueOf(10)).intValue());
    }

    userRepository.save(user);

    withdrawEvent.setStatus(OperationType.SUCCESS);
    kafkaTemplate.send("WALLET_EVENT", withdrawEvent);
  }

  @Override
  public void replenishmentMoney(CreateWalletEvent createWalletEvent) {
    User user = userRepository.findById(createWalletEvent.getUserId())
        .orElseThrow(() -> new UserNotFoundException(createWalletEvent.getUserId()));
    user.setWallet(user.getWallet().add(createWalletEvent.getCost()));
    userRepository.save(user);
    WithdrawEvent withdrawEvent = new WithdrawEvent();
    withdrawEvent.setPaymentId(createWalletEvent.getPaymentId());
    withdrawEvent.setStatus(OperationType.SUCCESS);
    kafkaTemplate.send("WALLET_EVENT", withdrawEvent);
  }

  @Override
  public void refundMoneys(RefundMoneyEvent refundMoneyEvent) {
    User user = userRepository.findById(refundMoneyEvent.getUserId())
        .orElseThrow(() -> new UserNotFoundException(refundMoneyEvent.getUserId()));
    user.setWallet(user.getWallet().add(refundMoneyEvent.getAmount()));
    userRepository.save(user);
    WithdrawEvent withdrawEvent = new WithdrawEvent();
    withdrawEvent.setPaymentId(refundMoneyEvent.getPaymentId());
    withdrawEvent.setStatus(OperationType.REFUNDED);
    kafkaTemplate.send("WALLET_EVENT", withdrawEvent);
  }

  @Override
  public UserDTO getUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    return userMapper.toDTO(user);
  }

}
