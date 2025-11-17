package com.actisys.billingservice.controller;

import com.actisys.billingservice.dto.TariffDtos.TariffDTO;
import com.actisys.billingservice.dto.TariffDtos.CreateTariffDTO;
import com.actisys.billingservice.service.TariffService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tariffs")
public class TariffController {
  private final TariffService tariffService;

  @GetMapping("/allTariffs")
  public ResponseEntity<List<TariffDTO>> getAllTariffs() {
    List<TariffDTO> tariffList = tariffService.getAllTariffs();
    return new ResponseEntity<>(tariffList, HttpStatus.OK);
  }

  @GetMapping("/getPopularTariff")
  public ResponseEntity<List<TariffDTO>> getPopularTariff() {
    List<TariffDTO> tariffList = tariffService.getPopularTariffs();
    return new ResponseEntity<>(tariffList, HttpStatus.OK);
  }

  @PostMapping("/createTariff")
  public ResponseEntity<TariffDTO> createTariff(@RequestBody CreateTariffDTO tariffForCreateDTO) {
    TariffDTO tariffDTO = tariffService.createTariff(tariffForCreateDTO);
    return new ResponseEntity<>(tariffDTO, HttpStatus.CREATED);
  }

  @PutMapping("/updateTariff/{id}")
  public ResponseEntity<TariffDTO> updateTariff(@PathVariable Long id, @RequestBody CreateTariffDTO updatedTariff) {
    TariffDTO tariffDTO = tariffService.updateTariff(id, updatedTariff);
    return new ResponseEntity<>(tariffDTO, HttpStatus.OK);
  }

  @DeleteMapping("/deleteTariff/{id}")
  public ResponseEntity<Void> deleteTariff(@PathVariable Long id) {
    tariffService.deleteTariff(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
