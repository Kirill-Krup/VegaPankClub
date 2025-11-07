package com.actisys.inventoryservice.mapper;

import com.actisys.common.dto.clientDtos.PcResponseDTO;
import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
import com.actisys.inventoryservice.dto.PcInfoDTO;
import com.actisys.inventoryservice.model.PC;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = RoomMapper.class)
public interface PcMapper {

  PCDTO toDTO(PC pc);

  PC toEntity(PCDTO pcDTO);

  PC toEntity(PcCreateDTO pcCreateDTO);

  @Mapping(source = "room", target = "room")
  PcInfoDTO toInfoDTO(PC pc);

  @Mapping(source = "room.name", target = "roomName")
  PcResponseDTO toResponseDto(PC pc);
}
