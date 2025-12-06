package com.actisys.userservice.service;

import com.actisys.common.events.user.CreateWalletEvent;
import com.actisys.common.events.user.RefundMoneyEvent;
import com.actisys.common.user.UserDTO;
import com.actisys.userservice.dto.UserResponseDtos.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import com.actisys.userservice.exception.InsufficientFundsException;
import com.actisys.userservice.exception.UserNotFoundException;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Service for managing users, their profiles, balances and wallet-related operations. [web:3]
 */
public interface UserService {

  /**
   * Updates basic user profile data such as full name, email and phone. [web:3]
   * Also invalidates the cached list of admin users to keep data consistent. [web:19]
   *
   * @param id user identifier
   * @param updated DTO with updated profile data
   * @return updated UserDTO
   * @throws UserNotFoundException if user with given id does not exist
   */
  UserDTO updateUser(Long id, UpdateUserProfileDTO updated);

  /**
   * Updates the path to the user's profile photo stored in external storage. [web:3]
   * After update, clears admin users cache so UI reflects new avatar. [web:19]
   *
   * @param id user identifier
   * @param photoPath new profile photo path or key
   * @return updated UserDTO with new photoPath
   * @throws UserNotFoundException if user with given id does not exist
   */
  UserDTO updateUserPhoto(Long id, String photoPath);

  /**
   * Permanently removes user from the system by identifier. [web:3]
   * Also evicts cached admin users list to avoid stale references. [web:19]
   *
   * @param id user identifier to delete
   * @throws UserNotFoundException if user with given id does not exist
   */
  void deleteUser(Long id);

  /**
   * Adjusts user's bonus coins balance by the specified amount. [web:3]
   * Positive value increases balance, negative value decreases it and cache is invalidated. [web:19]
   *
   * @param id user identifier
   * @param coins amount of coins to add or subtract
   * @return updated UserDTO with new bonus coins value
   * @throws UserNotFoundException if user with given id does not exist
   */
  UserDTO updateUserCoins(Long id, int coins);

  /**
   * Returns a lightweight user profile without external service data. [web:3]
   * Includes login, wallet balance, photo path, ban flag and role only. [web:3]
   *
   * @param userId user identifier
   * @return simple user profile DTO
   * @throws UserNotFoundException if user with given id does not exist
   */
  UserSimpleProfileDTO getProfile(Long userId);

  /**
   * Returns full user profile enriched with data from Billing Service. [web:3]
   * Wraps result into a Mono and caches external stats calls for a limited time. [web:16]
   *
   * @param userId user identifier
   * @return reactive Mono with complete user profile DTO
   * @throws UserNotFoundException if user with given id does not exist
   */
  Mono<UserAllProfileDTO> getAllProfile(Long userId);

  /**
   * Retrieves list of all users stored in the system. [web:3]
   * Intended for admin panel usage and typically cached at service layer. [web:19]
   *
   * @return list of all users as DTOs
   */
  List<UserDTO> getAllUsers();

  /**
   * Blocks user from accessing the system by setting ban flag. [web:3]
   * Administrators cannot be blocked and cache is cleared after update. [web:19]
   *
   * @param id user identifier to block
   * @return updated UserDTO reflecting ban status
   * @throws UserNotFoundException if user with given id does not exist
   * @throws RuntimeException if attempt is made to block an administrator
   */
  UserDTO blockUser(Long id);

  /**
   * Removes ban from user and restores normal access. [web:3]
   * Also invalidates admin users cache so status is refreshed in UI. [web:19]
   *
   * @param id user identifier to unblock
   * @return updated UserDTO with ban flag cleared
   * @throws UserNotFoundException if user with given id does not exist
   */
  UserDTO unBlockUser(Long id);

  /**
   * Returns current bonus coins balance for the given user. [web:3]
   *
   * @param id user identifier
   * @return integer value of user's bonus coins
   * @throws UserNotFoundException if user with given id does not exist
   */
  Integer myCoins(Long id);

  /**
   * Uploads user's profile photo file to storage and updates photo path. [web:3]
   * Combines storage upload and profile update into a single operation. [web:3]
   *
   * @param userId user identifier
   * @param file uploaded photo file
   * @return updated UserDTO with new photo path
   * @throws UserNotFoundException if user with given id does not exist
   */
  UserDTO uploadAndUpdateUserPhoto(Long userId, org.springframework.web.multipart.MultipartFile file);

  /**
   * Processes wallet withdrawal for a user according to incoming event. [web:9]
   * Decreases wallet balance, may add bonus coins, and publishes Kafka status event. [web:9]
   *
   * @param createWalletEvent event containing paymentId, userId and cost
   * @throws UserNotFoundException if user with given id does not exist
   * @throws InsufficientFundsException if wallet balance is too low
   */
  void withdrawMoney(CreateWalletEvent createWalletEvent);

  /**
   * Replenishes user's wallet by amount from the incoming event. [web:9]
   * Persists new balance and sends success status event to Kafka topic. [web:9]
   *
   * @param createWalletEvent event containing userId, cost and paymentId
   * @throws UserNotFoundException if user with given id does not exist
   */
  void replenishmentMoney(CreateWalletEvent createWalletEvent);

  /**
   * Refunds previously charged amount back to the user's wallet. [web:9]
   * Updates wallet balance and emits Kafka event with refunded status. [web:9]
   *
   * @param refundMoneyEvent event with refund amount, paymentId and userId
   * @throws UserNotFoundException if user with given id does not exist
   */
  void refundMoneys(RefundMoneyEvent refundMoneyEvent);

  /**
   * Retrieves a single user by identifier and maps it to DTO. [web:3]
   *
   * @param id user identifier
   * @return UserDTO for requested user
   * @throws UserNotFoundException if user with given id does not exist
   */
  UserDTO getUser(Long id);
}
