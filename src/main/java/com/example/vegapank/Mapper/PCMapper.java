package com.example.vegapank.Mapper;

import com.example.vegapank.DTO.PCDTO;
import com.example.vegapank.Model.PC;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PCMapper {

  PCDTO toDTO(PC pc);

  PC toEntity(PCDTO pcDTO);
}
