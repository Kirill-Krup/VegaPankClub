package com.actisys.userservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.service.UserService;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
@Rollback
class UserServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
      .withDatabaseName("users_test_db")
      .withUsername("test")
      .withPassword("test");

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
      .withExposedPorts(6379);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    registry.add("spring.liquibase.enabled", () -> "true");
    registry.add("spring.liquibase.change-log",
        () -> "classpath:/db/changelog/db.changelog-master.xml");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
  }

  @Autowired
  private UserService userService;

  @Autowired
  private CacheManager cacheManager;

  private int userCounter = 0;

  @BeforeEach
  void setup() {
    if (cacheManager.getCache("users") != null) {
      cacheManager.getCache("users").clear();
    }
    userCounter = 0;
  }

  private RegisterRequest createUniqueRegisterRequest() {
    userCounter++;
    return new RegisterRequest(
        "testuser" + userCounter,
        "StrongPassword123",
        "test" + userCounter + "@example.com",
        "+123456789" + userCounter,
        "Test User " + userCounter,
        new Timestamp(System.currentTimeMillis() - 100000L)
    );
  }

  @Test
  @DisplayName("Create user and get by id - should cache result")
  void createAndFetchUser_shouldCacheResult() {
    RegisterRequest request = createUniqueRegisterRequest();

    UserDTO created = userService.createUser(request);
    System.out.println("=== CREATED USER ===");
    System.out.println("ID: " + created.getId());
    System.out.println("Login: " + created.getLogin());
    System.out.println("Email: " + created.getEmail());

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getLogin()).isEqualTo("testuser1");
    assertThat(created.getEmail()).isEqualTo("test1@example.com");

    Long userId = created.getId();

    System.out.println("=== FIRST CALL (should cache) ===");
    Optional<UserDTO> firstCall = userService.getUserById(userId);
    System.out.println("First call present: " + firstCall.isPresent());
    if (firstCall.isPresent()) {
      System.out.println("First call ID: " + firstCall.get().getId());
    }

    assertThat(firstCall).isPresent();
    assertThat(firstCall.get().getId()).isEqualTo(userId);

    System.out.println("=== CHECKING CACHE ===");
    if (cacheManager.getCache("users") != null) {
      Object cachedValue = cacheManager.getCache("users").get(userId, Object.class);
      System.out.println("Cached value: " + cachedValue);
      if (cachedValue != null) {
        System.out.println("Cached value class: " + cachedValue.getClass());
      }
    }

    System.out.println("=== SECOND CALL (should use cache) ===");
    Optional<UserDTO> cachedCall = userService.getUserById(userId);
    System.out.println("Second call present: " + cachedCall.isPresent());
    if (cachedCall.isPresent()) {
      System.out.println("Second call ID: " + cachedCall.get().getId());
    } else {
      System.out.println("Second call returned empty!");
    }

    assertThat(cachedCall).isPresent();
    assertThat(cachedCall.get().getId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("Update user - should update cache")
  void updateUser_shouldUpdateCache() {
    RegisterRequest request = createUniqueRegisterRequest();
    UserDTO created = userService.createUser(request);
    Long userId = created.getId();
    UserDTO updateDto = new UserDTO(
        userId,
        created.getLogin(),
        created.getEmail(),
        created.getPhone(),
        "Updated User Name",
        created.getWallet(),
        created.getPhotoPath(),
        created.getBonusCoins(),
        created.getRegistrationDate(),
        created.getBirthDate(),
        created.getLastLogin(),
        null,
        null
    );

    UserDTO updated = userService.updateUser(userId, updateDto);
    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(userId);
    assertThat(updated.getFullName()).isEqualTo("Updated User Name");

    Optional<UserDTO> fromCache = userService.getUserById(userId);

    assertThat(fromCache).isPresent();
    assertThat(fromCache.get().getFullName()).isEqualTo("Updated User Name");
  }

  @Test
  @DisplayName("Delete user - should remove from cache")
  void deleteUser_shouldRemoveFromCache() {
    RegisterRequest request = createUniqueRegisterRequest();
    UserDTO created = userService.createUser(request);
    Long userId = created.getId();
    userService.getUserById(userId);
    UserDTO deleted = userService.deleteUser(userId);
    assertThat(deleted).isNotNull();
    assertThat(deleted.getId()).isEqualTo(userId);
    Optional<UserDTO> afterDeletion = userService.getUserById(userId);
    assertThat(afterDeletion).isEmpty();
  }

  @Test
  @DisplayName("Get user by email - should use cache")
  void getUserByEmail_shouldUseCache() {
    RegisterRequest request = createUniqueRegisterRequest();
    UserDTO created = userService.createUser(request);
    String email = created.getEmail();

    UserDTO firstCall = userService.getUserByEmail(email);

    assertThat(firstCall).isNotNull();
    assertThat(firstCall.getEmail()).isEqualTo(email);

    UserDTO secondCall = userService.getUserByEmail(email);

    assertThat(secondCall).isNotNull();
    assertThat(secondCall.getEmail()).isEqualTo(email);
  }

  @Test
  @DisplayName("Update user coins - should update bonus coins")
  void updateUserCoins_shouldUpdateBonusCoins() {
    RegisterRequest request = createUniqueRegisterRequest();
    UserDTO created = userService.createUser(request);
    Long userId = created.getId();
    int initialCoins = created.getBonusCoins();

    UserDTO updated = userService.updateUserCoins(userId, 100);

    assertThat(updated).isNotNull();
    assertThat(updated.getBonusCoins()).isEqualTo(initialCoins + 100);

    Optional<UserDTO> fromDb = userService.getUserById(userId);

    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getBonusCoins()).isEqualTo(initialCoins + 100);
  }

  @Test
  @DisplayName("Get users by multiple ids - should return existing users")
  void getUsersByIds_shouldReturnMultipleUsers() {
    UserDTO user1 = userService.createUser(createUniqueRegisterRequest());
    UserDTO user2 = userService.createUser(createUniqueRegisterRequest());

    List<UserDTO> users = userService.getUsersByIds(List.of(user1.getId(), user2.getId()));

    assertThat(users).hasSize(2);
    assertThat(users).extracting(UserDTO::getId)
        .containsExactlyInAnyOrder(user1.getId(), user2.getId());
  }

  @Test
  @DisplayName("Get user by non-existing id - should return empty optional")
  void getUserByNonExistingId_shouldReturnEmpty() {
    Optional<UserDTO> result = userService.getUserById(999L);
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Get user by non-existing email - should throw exception")
  void getUserByNonExistingEmail_shouldThrowException() {
    assertThatThrownBy(() -> userService.getUserByEmail("nonexisting@example.com"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("nonexisting@example.com");
  }

  @Test
  @DisplayName("Containers should be running")
  void containers_shouldBeRunning() {
    assertThat(postgres.isRunning()).isTrue();
    assertThat(redis.isRunning()).isTrue();
  }
}