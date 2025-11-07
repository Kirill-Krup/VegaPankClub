package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.RoomDTO;
import com.actisys.inventoryservice.dto.RoomUpdateDTO;
import com.actisys.inventoryservice.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

  @Mapping(source = "vip", target = "isVip")
  RoomDTO toDTO(Room room);

  Room toEntity(RoomDTO roomDTO);
}
