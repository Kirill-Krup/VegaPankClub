package com.actisys.billingservice.mapper;

import com.actisys.billingservice.dto.SessionDTO;
import com.actisys.billingservice.model.Session;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {

  Session toEntity(SessionDTO sessionDTO);

  SessionDTO toDTO(Session session);
}
