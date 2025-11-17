package com.actisys.billingservice.service.impl;

import com.actisys.billingservice.dto.TariffDtos.CreateTariffDTO;
import com.actisys.billingservice.dto.TariffDtos.TariffDTO;
import com.actisys.billingservice.exception.TariffAlreadyExistsException;
import com.actisys.billingservice.exception.TariffNotFoundException;
import com.actisys.billingservice.mapper.TariffMapper;
import com.actisys.billingservice.model.Tariff;
import com.actisys.billingservice.repository.TariffRepository;
import com.actisys.billingservice.service.TariffService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {

  private final TariffRepository tariffRepository;
  private final TariffMapper tariffMapper;

  @Override
  @Cacheable(value = "allTariffs", key = "'all'")
  public List<TariffDTO> getAllTariffs() {
    List<Tariff> tariffs = tariffRepository.findAll();
    return tariffs.stream().map(tariffMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(value = "allTariffs", allEntries = true)
  public TariffDTO createTariff(CreateTariffDTO tariffForCreateDTO) {
    if(tariffRepository.existsByName(tariffForCreateDTO.getName())) {
      throw new TariffAlreadyExistsException(tariffForCreateDTO.getName());
    }
    Tariff tariff = Tariff.builder()
        .name(tariffForCreateDTO.getName())
        .price(tariffForCreateDTO.getPrice())
        .isVip(tariffForCreateDTO.isVip())
        .hours(tariffForCreateDTO.getHours())
        .build();
    Tariff createdTariff = tariffRepository.save(tariff);
    return tariffMapper.toDTO(createdTariff);
  }

  @Override
  @Transactional
  @CacheEvict(value = "allTariffs", allEntries = true)
  public TariffDTO updateTariff(Long id, CreateTariffDTO updatedTariff) {
    Tariff tariff = tariffRepository.findById(id).orElseThrow(()->new TariffNotFoundException(id));
    if(!tariff.getName().equals(updatedTariff.getName())) {
      if(tariffRepository.existsByName(updatedTariff.getName())) {
        throw new TariffAlreadyExistsException(updatedTariff.getName());
      }
    }
    tariff.setName(updatedTariff.getName());
    tariff.setPrice(updatedTariff.getPrice());
    tariff.setIsVip(updatedTariff.isVip());
    tariff.setHours(updatedTariff.getHours());
    return tariffMapper.toDTO(tariffRepository.save(tariff));
  }

  @Override
  @Transactional
  @CacheEvict(value = "allTariffs", allEntries = true)
  public void deleteTariff(Long id) {
    Tariff tariff = tariffRepository.findById(id)
        .orElseThrow(() -> new TariffNotFoundException(id));
    tariffRepository.delete(tariff);
  }

  @Override
  @Cacheable(value = "allTariffs", key = "'top3'")
  public List<TariffDTO> getPopularTariffs() {
    List<Tariff> tariffs = tariffRepository.findPopularTariffs(Limit.of(3));
    if(tariffs.size() < 3){
      Set<Tariff> uniqueTariffs = new LinkedHashSet<>(tariffs);
      uniqueTariffs.addAll(tariffRepository.findFirst3By());
      tariffs = uniqueTariffs.stream()
          .limit(3)
          .toList();
    }
    return tariffs.stream().map(tariffMapper::toDTO).collect(Collectors.toList());
  }
}
