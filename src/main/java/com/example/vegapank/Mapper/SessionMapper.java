package com.example.vegapank.Mapper;
import com.example.vegapank.DTO.SessionDTO;
import com.example.vegapank.Model.Session;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {

  SessionDTO toDTO(Session session);

  Session toEntity(SessionDTO sessionDTO);
}
