package com.example.vegapank.Mapper;

import com.example.vegapank.DTO.UserDTO;
import org.mapstruct.Mapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDTO toDTO(User user);

  User toEntity(UserDTO userDTO);
}
