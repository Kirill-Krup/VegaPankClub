package com.example.vegapank.Mapper;

import com.example.vegapank.DTO.TariffDTO;
import com.example.vegapank.Model.Tariff;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TariffMapper {

  TariffDTO toDTO(Tariff tariff);

  Tariff toEntity(TariffDTO tariffDTO);
}
