package com.actisys.userservice.mapper;

import com.actisys.userservice.dto.UserDTO;
import com.actisys.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "password", ignore = true)
  UserDTO toDTO(User user);

  @Mapping(target = "password", ignore = true)
  User toEntity(UserDTO dto);
}