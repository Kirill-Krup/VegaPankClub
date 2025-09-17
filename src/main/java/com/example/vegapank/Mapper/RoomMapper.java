package com.example.vegapank.Mapper;
import com.example.vegapank.DTO.RoomDTO;
import com.example.vegapank.Model.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {

  RoomDTO toDTO(Room room);

  Room toEntity(RoomDTO roomDTO);
}
