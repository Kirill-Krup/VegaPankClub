package com.actisys.userservice.UnitTests;

import com.actisys.common.user.UserDTO;
import com.actisys.userservice.client.BillingServiceClient;
import com.actisys.userservice.dto.UserResponseDtos.UpdateUserProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserAllProfileDTO;
import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private BillingServiceClient billingServiceClient;

  @InjectMocks
  private UserServiceImpl userService;

  private User testUser;
  private UserDTO testUserDTO;
  private UpdateUserProfileDTO updateDTO;
  private Timestamp testTimestamp;

  @BeforeEach
  void setUp() {
    testTimestamp = new Timestamp(System.currentTimeMillis());

    testUser = new User();
    testUser.setId(1L);
    testUser.setLogin("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPhone("1234567890");
    testUser.setFullName("Test User");
    testUser.setWallet(100.0);
    testUser.setBonusCoins(50);
    testUser.setPhotoPath("/photos/test.jpg");
    testUser.setBanned(false);
    testUser.setRole(1);
    testUser.setRegistrationDate(testTimestamp);

    testUserDTO = new UserDTO();
    testUserDTO.setId(1L);
    testUserDTO.setLogin("testuser");
    testUserDTO.setEmail("test@example.com");
    testUserDTO.setPhone("1234567890");
    testUserDTO.setFullName("Test User");
    testUserDTO.setWallet(100.0);
    testUserDTO.setBonusCoins(50);
    testUserDTO.setBanned(false);
    testUserDTO.setRole("USER");

    updateDTO = UpdateUserProfileDTO.builder()
        .fullName("Updated User")
        .email("updated@example.com")
        .phone("0987654321")
        .build();
  }


  @Test
  @DisplayName("Should update user profile successfully")
  void testUpdateUserSuccess() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    UserDTO result = userService.updateUser(1L, updateDTO);

    assertNotNull(result);
    assertEquals("testuser", result.getLogin());
    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).save(any(User.class));
    verify(userMapper, times(1)).toDTO(testUser);
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when updating non-existent user")
  void testUpdateUserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updateDTO));
    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update user fields correctly")
  void testUpdateUserFieldsCorrectly() {
    User userToUpdate = new User();
    userToUpdate.setId(1L);
    userToUpdate.setLogin("testuser");
    userToUpdate.setEmail("old@example.com");
    userToUpdate.setPhone("1111111111");
    userToUpdate.setFullName("Old Name");

    when(userRepository.findById(1L)).thenReturn(Optional.of(userToUpdate));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

    userService.updateUser(1L, updateDTO);

    assertEquals("Updated User", userToUpdate.getFullName());
    assertEquals("updated@example.com", userToUpdate.getEmail());
    assertEquals("0987654321", userToUpdate.getPhone());
  }


  @Test
  @DisplayName("Should delete user successfully")
  void testDeleteUserSuccess() {
    when(userRepository.existsById(1L)).thenReturn(true);

    userService.deleteUser(1L);

    verify(userRepository, times(1)).existsById(1L);
    verify(userRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
  void testDeleteUserNotFound() {
    when(userRepository.existsById(1L)).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    verify(userRepository, times(1)).existsById(1L);
    verify(userRepository, never()).deleteById(any());
  }


  @Test
  @DisplayName("Should add coins to user balance")
  void testUpdateUserCoinsAddSuccess() {
    User userWithCoins = new User();
    userWithCoins.setId(1L);
    userWithCoins.setBonusCoins(50);

    UserDTO expectedDTO = new UserDTO();
    expectedDTO.setId(1L);
    expectedDTO.setBonusCoins(75);

    when(userRepository.findById(1L)).thenReturn(Optional.of(userWithCoins));
    when(userRepository.save(any(User.class))).thenReturn(userWithCoins);
    when(userMapper.toDTO(userWithCoins)).thenReturn(expectedDTO);

    UserDTO result = userService.updateUserCoins(1L, 25);

    assertEquals(75, userWithCoins.getBonusCoins());
    assertNotNull(result);
    assertEquals(75, result.getBonusCoins());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should deduct coins from user balance")
  void testUpdateUserCoinsDeductSuccess() {
    User userWithCoins = new User();
    userWithCoins.setId(1L);
    userWithCoins.setBonusCoins(50);

    UserDTO expectedDTO = new UserDTO();
    expectedDTO.setId(1L);
    expectedDTO.setBonusCoins(30);

    when(userRepository.findById(1L)).thenReturn(Optional.of(userWithCoins));
    when(userRepository.save(any(User.class))).thenReturn(userWithCoins);
    when(userMapper.toDTO(userWithCoins)).thenReturn(expectedDTO);

    userService.updateUserCoins(1L, -20);

    assertEquals(30, userWithCoins.getBonusCoins());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when updating coins for non-existent user")
  void testUpdateUserCoinsNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUserCoins(1L, 50));
    verify(userRepository, never()).save(any());
  }


  @Test
  @DisplayName("Should update user photo successfully")
  void testUpdateUserPhotoSuccess() {
    User userForPhoto = new User();
    userForPhoto.setId(1L);
    userForPhoto.setPhotoPath("/old/photo.jpg");

    when(userRepository.findById(1L)).thenReturn(Optional.of(userForPhoto));
    when(userRepository.save(any(User.class))).thenReturn(userForPhoto);
    when(userMapper.toDTO(userForPhoto)).thenReturn(testUserDTO);

    UserDTO result = userService.updateUserPhoto(1L, "/new/photo.jpg");

    assertEquals("/new/photo.jpg", userForPhoto.getPhotoPath());
    assertNotNull(result);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when updating photo for non-existent user")
  void testUpdateUserPhotoNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUserPhoto(1L, "/photo.jpg"));
    verify(userRepository, never()).save(any());
  }


  @Test
  @DisplayName("Should return simple profile successfully")
  void testGetProfileSuccess() {
    UserSimpleProfileDTO simpleProfile = UserSimpleProfileDTO.builder()
        .login("testuser")
        .wallet(100.0)
        .photoPath("/photos/test.jpg")
        .isBanned(false)
        .role(1)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    UserSimpleProfileDTO result = userService.getProfile(1L);

    assertNotNull(result);
    assertEquals("testuser", result.getLogin());
    assertEquals(100.0, result.getWallet());
    assertFalse(result.isBanned());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when getting profile for non-existent user")
  void testGetProfileNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getProfile(1L));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when getting full profile for non-existent user")
  void testGetAllProfileUserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Mono<UserAllProfileDTO> result = userService.getAllProfile(1L);

    assertThrows(UserNotFoundException.class, result::block);
  }

  @Test
  @DisplayName("Should return profile even if billing service fails")
  void testGetAllProfileBillingServiceError() {
    UserAllProfileDTO fullProfile = UserAllProfileDTO.builder()
        .login("testuser")
        .fullName("Test User")
        .email("test@example.com")
        .phone("1234567890")
        .wallet(100.0)
        .photoPath("/photos/test.jpg")
        .bonusCoins(50)
        .registrationDate(testTimestamp)
        .birthDate(null)
        .sessionStats(null)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userMapper.toAllProfileDTO(testUser)).thenReturn(fullProfile);
    when(billingServiceClient.getUserSessionStats(1L))
        .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

    Mono<UserAllProfileDTO> result = userService.getAllProfile(1L);

    UserAllProfileDTO profile = result.block();
    assertNotNull(profile);
    assertEquals("testuser", profile.getLogin());
    verify(billingServiceClient, times(1)).getUserSessionStats(1L);
  }


  @Test
  @DisplayName("Should return list of all users")
  void testGetAllUsersSuccess() {
    List<User> userList = List.of(testUser);

    when(userRepository.findAllUsers()).thenReturn(userList);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

    List<UserDTO> result = userService.getAllUsers();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("testuser", result.get(0).getLogin());
    verify(userRepository, times(1)).findAllUsers();
  }

  @Test
  @DisplayName("Should return empty list when no users exist")
  void testGetAllUsersEmpty() {
    when(userRepository.findAllUsers()).thenReturn(List.of());

    List<UserDTO> result = userService.getAllUsers();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(userRepository, times(1)).findAllUsers();
  }

  @Test
  @DisplayName("Should map multiple users correctly")
  void testGetAllUsersMultiple() {
    User user2 = new User();
    user2.setId(2L);
    user2.setLogin("testuser2");
    user2.setEmail("test2@example.com");

    UserDTO userDTO2 = new UserDTO();
    userDTO2.setId(2L);
    userDTO2.setLogin("testuser2");
    userDTO2.setEmail("test2@example.com");

    List<User> userList = List.of(testUser, user2);

    when(userRepository.findAllUsers()).thenReturn(userList);
    when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
    when(userMapper.toDTO(user2)).thenReturn(userDTO2);

    List<UserDTO> result = userService.getAllUsers();

    assertEquals(2, result.size());
    assertEquals("testuser", result.get(0).getLogin());
    assertEquals("testuser2", result.get(1).getLogin());
    verify(userRepository, times(1)).findAllUsers();
  }


  @Test
  @DisplayName("Should block user successfully")
  void testBlockUserSuccess() {
    User userToBlock = new User();
    userToBlock.setId(1L);
    userToBlock.setRole(1);
    userToBlock.setBanned(false);

    when(userRepository.findById(1L)).thenReturn(Optional.of(userToBlock));
    when(userRepository.save(any(User.class))).thenReturn(userToBlock);
    when(userMapper.toDTO(userToBlock)).thenReturn(testUserDTO);

    UserDTO result = userService.blockUser(1L);

    assertTrue(userToBlock.isBanned());
    assertNotNull(result);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw exception when blocking admin")
  void testBlockAdminThrowsException() {
    User adminUser = new User();
    adminUser.setId(2L);
    adminUser.setRole(2);
    adminUser.setBanned(false);

    when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

    assertThrows(RuntimeException.class, () -> userService.blockUser(2L));
    assertFalse(adminUser.isBanned());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when blocking non-existent user")
  void testBlockUserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.blockUser(1L));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should unblock user successfully")
  void testUnBlockUserSuccess() {
    User userToUnBlock = new User();
    userToUnBlock.setId(1L);
    userToUnBlock.setBanned(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(userToUnBlock));
    when(userRepository.save(any(User.class))).thenReturn(userToUnBlock);
    when(userMapper.toDTO(userToUnBlock)).thenReturn(testUserDTO);

    UserDTO result = userService.unBlockUser(1L);

    assertFalse(userToUnBlock.isBanned());
    assertNotNull(result);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when unblocking non-existent user")
  void testUnBlockUserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.unBlockUser(1L));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should handle already unblocked user")
  void testUnBlockAlreadyUnblockedUser() {
    User userAlreadyUnblocked = new User();
    userAlreadyUnblocked.setId(1L);
    userAlreadyUnblocked.setBanned(false);

    when(userRepository.findById(1L)).thenReturn(Optional.of(userAlreadyUnblocked));
    when(userRepository.save(any(User.class))).thenReturn(userAlreadyUnblocked);
    when(userMapper.toDTO(userAlreadyUnblocked)).thenReturn(testUserDTO);

    UserDTO result = userService.unBlockUser(1L);

    assertFalse(userAlreadyUnblocked.isBanned());
    assertNotNull(result);
    verify(userRepository, times(1)).save(any(User.class));
  }
}
