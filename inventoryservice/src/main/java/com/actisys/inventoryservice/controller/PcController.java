package com.actisys.inventoryservice.controller;

import com.actisys.common.dto.clientDtos.PcResponseDTO;
import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
import com.actisys.inventoryservice.dto.PcInfoDTO;
import com.actisys.inventoryservice.dto.PcUpdateDTO;
import com.actisys.inventoryservice.service.PcService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pcs")
@RequiredArgsConstructor
public class PcController {

  private final PcService pcService;

  @GetMapping("/allPc")
  public ResponseEntity<List<PcInfoDTO>> getAllPc() {
    List<PcInfoDTO> allPc = pcService.getAllPc();
    return ResponseEntity.ok(allPc);
  }

  @GetMapping("/pcInfo/{id}")
  public ResponseEntity<PcResponseDTO> getPcInfo(@PathVariable Long id) {
    PcResponseDTO responseDTO = pcService.getPcInfoById(id);
    return ResponseEntity.ok(responseDTO);
  }

  @GetMapping("/pcInfoByIds")
  public ResponseEntity<List<PcResponseDTO>> getPcInfoByIds(@RequestParam List<Long> ids) {
    List<PcResponseDTO> responseDTOS = pcService.getPcsByIds(ids);
    return ResponseEntity.ok(responseDTOS);
  }

  @PutMapping("/disablePs/{id}")
  public ResponseEntity<PCDTO> disablePs(@PathVariable Long id) {
    PCDTO dto = pcService.disablePs(id);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/activatePs/{id}")
  public ResponseEntity<PCDTO> activatePs(@PathVariable Long id) {
    PCDTO dto = pcService.activatePs(id);
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/addNewPc")
  public ResponseEntity<PCDTO> addNewPc(@RequestBody PcCreateDTO pcCreateDTO) {
    PCDTO pcDto = pcService.addNewPc(pcCreateDTO);
    return ResponseEntity.ok(pcDto);
  }

  @PutMapping("/updatePc/{id}")
  public ResponseEntity<PCDTO> updatePc(@PathVariable Long id, @RequestBody PcUpdateDTO pcUpdateDTO) {
    PCDTO pcDto = pcService.updatePc(id, pcUpdateDTO);
    return ResponseEntity.ok(pcDto);
  }

  @DeleteMapping("/deletePc/{id}")
  public ResponseEntity<Void> deletePc(@PathVariable Long id) {
    pcService.deletePc(id);
    return ResponseEntity.noContent().build();
  }

}
