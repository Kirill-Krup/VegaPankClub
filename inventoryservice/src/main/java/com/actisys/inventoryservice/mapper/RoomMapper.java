package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.RoomDTO;
import com.actisys.inventoryservice.dto.RoomUpdateDTO;
import com.actisys.inventoryservice.model.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PcMapper.class)
public interface RoomMapper {

  RoomDTO toDTO(Room room);

  Room toEntity(RoomDTO roomDTO);
}
