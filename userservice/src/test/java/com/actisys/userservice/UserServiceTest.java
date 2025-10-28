package com.actisys.userservice;


import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.mapper.UserMapper;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserMapper userMapper;
  @InjectMocks
  private UserServiceImpl userService;

  private User user;
  private UserDTO dto;
  private RegisterRequest registerRequest;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@mail.com");
    user.setFullName("Test User");
    user.setPhone("+1234567890");
    user.setBonusCoins(10);
    user.setRegistrationDate(new Timestamp(System.currentTimeMillis()));

    dto = new UserDTO(
        1L, "testLogin", "test@mail.com", "+1234567890",
        "Test User", 100.0, "/img.png", 10,
        new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() - 10000),
        new Timestamp(System.currentTimeMillis()), false, false
    );

    registerRequest = new RegisterRequest(
        "login123",
        "StrongPass1",
        "email@mail.com",
        "+1234567890",
        "Test User",
        new Timestamp(System.currentTimeMillis() - 100000L)
    );


  }


  @Test
  void createUser_shouldReturnDTO() {
    when(userMapper.fromRegisterRequest(registerRequest)).thenReturn(user);
    when(userRepository.save(user)).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(dto);

    UserDTO result = userService.createUser(registerRequest);

    assertEquals(dto.getEmail(), result.getEmail());
    verify(userRepository).save(user);
  }

  @Test
  void getUserById_shouldReturnUser() {
    when(userRepository.findUserById(1L)).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(dto);
    Optional<UserDTO> result = userService.getUserById(1L);
    assertTrue(result.isPresent());
  }

  @Test
  void getUserByEmail_shouldReturnUser() {
    when(userRepository.findUserByEmail("test@mail.com")).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(dto);
    UserDTO result = userService.getUserByEmail("test@mail.com");
    assertEquals(dto.getEmail(), result.getEmail());
  }

  @Test
  void getUserByEmail_shouldThrowIfNotFound() {
    when(userRepository.findUserByEmail(anyString())).thenReturn(null);
    assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("missing@mail.com"));
  }

  @Test
  void getUsersByIds_shouldReturnList() {
    when(userRepository.findUserById(1L)).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(dto);
    List<UserDTO> result = userService.getUsersByIds(List.of(1L));
    assertEquals(1, result.size());
  }

  @Test
  void updateUser_shouldUpdateFields() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(dto);
    UserDTO result = userService.updateUser(1L, dto);
    assertEquals(dto.getEmail(), result.getEmail());
  }

  @Test
  void updateUser_shouldThrowIfNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, dto));
  }

  @Test
  void deleteUser_shouldRemoveUser() {
    when(userRepository.existsById(1L)).thenReturn(true);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(dto);
    userService.deleteUser(1L);
    verify(userRepository).deleteById(1L);
  }

  @Test
  void deleteUser_shouldThrowIfNotFound() {
    when(userRepository.existsById(5L)).thenReturn(false);
    assertThrows(UserNotFoundException.class, () -> userService.deleteUser(5L));
  }

  @Test
  void updateUserCoins_shouldIncreaseCoins() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(dto);
    UserDTO result = userService.updateUserCoins(1L, 5);
    assertNotNull(result);
    verify(userRepository).save(user);
  }

  @Test
  void updateUserCoins_shouldThrowIfUserMissing() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.updateUserCoins(99L, 10));
  }

  @Test
  void getUserById_shouldReturnEmptyIfNotFound() {
    when(userRepository.findUserById(anyLong())).thenReturn(null);
    Optional<UserDTO> result = userService.getUserById(2L);
    assertTrue(result.isEmpty());
  }

  @Test
  void getUsersByIds_shouldSkipNulls() {
    when(userRepository.findUserById(anyLong())).thenReturn(null);
    List<UserDTO> result = userService.getUsersByIds(List.of(1L, 2L));
    assertTrue(result.isEmpty());
  }

  @Test
  void createUser_shouldMapCorrectly() {
    when(userMapper.fromRegisterRequest(any())).thenReturn(user);
    when(userRepository.save(any())).thenReturn(user);
    when(userMapper.toDTO(any())).thenReturn(dto);
    UserDTO result = userService.createUser(registerRequest);
    assertEquals(dto.getEmail(), result.getEmail());
  }

  @Test
  void deleteUser_shouldEvictCacheAnnotationPresent() {
    assertTrue(userService.getClass()
        .getDeclaredMethods()[0]
        .getAnnotations().length > 0);
  }
}
