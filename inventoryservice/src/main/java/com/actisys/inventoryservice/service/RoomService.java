package com.actisys.inventoryservice.service;

import com.actisys.inventoryservice.dto.RoomDTO;
import com.actisys.inventoryservice.dto.RoomUpdateDTO;
import java.util.List;

public interface RoomService {

  List<RoomDTO> getAllRooms();

  RoomDTO updateRoom(Long id, RoomUpdateDTO roomUpdateDTO);
}
