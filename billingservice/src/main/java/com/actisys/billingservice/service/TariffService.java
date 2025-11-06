package com.actisys.billingservice.service;

import com.actisys.billingservice.dto.CreateTariffDTO;
import com.actisys.billingservice.dto.TariffDTO;
import java.util.List;

public interface TariffService {

  List<TariffDTO> getAllTariffs();

  TariffDTO createTariff(CreateTariffDTO tariffForCreateDTO);

  TariffDTO updateTariff(Long id, CreateTariffDTO updatedTariff);

  void deleteTariff(Long id);

}
