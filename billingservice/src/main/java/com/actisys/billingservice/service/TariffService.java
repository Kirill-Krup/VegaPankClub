package com.actisys.billingservice.service;

import com.actisys.billingservice.dto.TariffDtos.CreateTariffDTO;
import com.actisys.billingservice.dto.TariffDtos.TariffDTO;
import java.util.List;

public interface TariffService {

  List<TariffDTO> getAllTariffs();

  TariffDTO createTariff(CreateTariffDTO tariffForCreateDTO);

  TariffDTO updateTariff(Long id, CreateTariffDTO updatedTariff);

  void deleteTariff(Long id);

}
