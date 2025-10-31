package com.actisys.userservice.repository;

import com.actisys.userservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findUserById(@Param("id") Long id);
  User findUserByLogin(@Param("login") String login);
  User findUserByEmail(@Param("email") String email);

  boolean existsByLogin(String login);

  boolean existsByEmail(@NotBlank(message = "Email cannot be blank") @Email(message = "Email must be valid") String email);

  Optional<User> findByLogin(@NotBlank(message = "Login cannot be blank") String login);

  Optional<User> findByEmail(@NotBlank(message = "Login cannot be blank") String login);

  @Query("SELECT u FROM User u WHERE u.role=1 ")
  List<User> findAllUsers();
}
