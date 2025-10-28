package com.actisys.inventoryservice.controller;

import com.actisys.inventoryservice.dto.RoomDTO;
import com.actisys.inventoryservice.dto.RoomUpdateDTO;
import com.actisys.inventoryservice.service.RoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {
  private final RoomService roomService;

  @GetMapping("/getAllRooms")
  public ResponseEntity<List<RoomDTO>> getAllRooms() {
    List<RoomDTO> allRooms = roomService.getAllRooms();
    return new ResponseEntity<>(allRooms, HttpStatus.OK);
  }

  @PutMapping("/updateRoom/{id}")
  public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id,@RequestBody RoomUpdateDTO roomUpdateDTO) {
    RoomDTO dto= roomService.updateRoom(id, roomUpdateDTO);
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }
}
