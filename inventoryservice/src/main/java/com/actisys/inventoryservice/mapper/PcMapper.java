package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.model.PC;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PcMapper {

  PCDTO toDTO(PC pc);

  PC toEntity(PCDTO pcDTO);
}
