package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.RoomDTO;
import com.actisys.inventoryservice.dto.RoomUpdateDTO;
import com.actisys.inventoryservice.exception.RoomNotFoundException;
import com.actisys.inventoryservice.mapper.RoomMapper;
import com.actisys.inventoryservice.model.Room;
import com.actisys.inventoryservice.repository.RoomRepository;
import com.actisys.inventoryservice.service.RoomService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

  private final RoomRepository roomRepository;
  private final RoomMapper roomMapper;

  @Override
  public List<RoomDTO> getAllRooms() {
    List<Room> rooms = roomRepository.findAll();
    return rooms.stream().map(roomMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  public RoomDTO updateRoom(Long id, RoomUpdateDTO roomUpdateDTO) {
    if(!roomRepository.existsById(id)) {
      throw new RoomNotFoundException(id);
    }
    Room room = roomRepository.findById(id).get();
    room.setName(roomUpdateDTO.getName());
    room.setVip(roomUpdateDTO.isVip());
    return roomMapper.toDTO(roomRepository.save(room));
  }

}
