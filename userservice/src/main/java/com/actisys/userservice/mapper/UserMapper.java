package com.actisys.userservice.mapper;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.userservice.dto.RegisterRequest;
import com.actisys.userservice.model.User;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDTO toDTO(User user);

  User toEntity(UserDTO dto);

  User fromRegisterRequest(RegisterRequest registerRequest);

}