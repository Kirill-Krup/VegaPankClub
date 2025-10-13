package com.actisys.billingservice.mapper;

import com.actisys.billingservice.dto.TariffDTO;
import com.actisys.billingservice.model.Tariff;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TariffMapper {

  Tariff toEntity(TariffDTO tariffDTO);

  TariffDTO toDTO(Tariff tariff);
}
