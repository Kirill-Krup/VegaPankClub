package com.actisys.billingservice.service;

import com.actisys.billingservice.dto.TariffDtos.CreateTariffDTO;
import com.actisys.billingservice.dto.TariffDtos.TariffDTO;
import java.util.List;

public interface TariffService {

  /**
   * Retrieves all available tariffs from cache.
   * Uses "all" key for efficient repeated access.
   *
   * @return list of all tariffs as DTOs
   */
  List<TariffDTO> getAllTariffs();

  /**
   * Creates new tariff with unique name validation.
   * Invalidates all tariffs cache after creation.
   *
   * @param tariffForCreateDTO tariff data (name, price, hours, VIP flag)
   * @return created TariffDTO
   */
  TariffDTO createTariff(CreateTariffDTO tariffForCreateDTO);

  /**
   * Updates existing tariff details with name uniqueness check.
   * Only validates name if it changed, clears cache after update.
   *
   * @param id tariff identifier to update
   * @param updatedTariff new tariff data
   * @return updated TariffDTO
   */
  TariffDTO updateTariff(Long id, CreateTariffDTO updatedTariff);

  /**
   * Permanently deletes tariff by identifier.
   * Clears all tariffs cache to maintain consistency.
   *
   * @param id tariff identifier to delete
   */
  void deleteTariff(Long id);

  /**
   * Returns top 3 popular tariffs, supplements with random if less than 3 available.
   * Cached with "top3" key for frontend performance.
   *
   * @return list of up to 3 most popular tariffs
   */
  List<TariffDTO> getPopularTariffs();
}
