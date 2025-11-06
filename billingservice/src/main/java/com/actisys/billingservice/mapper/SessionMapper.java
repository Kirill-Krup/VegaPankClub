package com.actisys.billingservice.mapper;

import com.actisys.billingservice.dto.SessionDTO;
import com.actisys.billingservice.dto.SessionsInfoDTO;
import com.actisys.billingservice.model.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {

  Session toEntity(SessionDTO sessionDTO);

  SessionDTO toDTO(Session session);

  SessionsInfoDTO toInfoDTO(Session session);
}
