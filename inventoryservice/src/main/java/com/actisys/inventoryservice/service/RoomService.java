package com.actisys.inventoryservice.service;

import com.actisys.inventoryservice.dto.RoomDTO;
import com.actisys.inventoryservice.dto.RoomUpdateDTO;
import java.util.List;

public interface RoomService {

  /**
   * Retrieves all rooms available in the inventory system.
   *
   * @return list of all rooms as DTOs
   */
  List<RoomDTO> getAllRooms();

  /**
   * Updates existing room name and VIP status flag.
   *
   * @param id room identifier to update
   * @param roomUpdateDTO new room name and VIP status
   * @return updated RoomDTO
   */
  RoomDTO updateRoom(Long id, RoomUpdateDTO roomUpdateDTO);
}
