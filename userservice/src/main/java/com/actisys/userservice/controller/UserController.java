package com.actisys.userservice.controller;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.AuthRequest;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.dto.UpdateCoinsRequest;
import com.actisys.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/getAllUsers")
  @PreAuthorize("hasRole='ADMIN'")
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<UserDTO> usersList = userService.getAllUsers();
    return new ResponseEntity<>(usersList, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    return userService.getUserById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.getUserByEmail(email));
  }

  @GetMapping("/ids")
  public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestParam List<Long> ids) {
    return ResponseEntity.ok(userService.getUsersByIds(ids));
  }

  @PutMapping("/{id}/coins")
  public ResponseEntity<UserDTO> updateUserCoins(@PathVariable Long id, @RequestBody UpdateCoinsRequest request) {
    UserDTO updated = userService.updateUserCoins(id, request.getCoins());
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
