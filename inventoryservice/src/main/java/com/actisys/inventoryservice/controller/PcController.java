package com.actisys.inventoryservice.controller;

import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pcs")
@RequiredArgsConstructor
public class PcController {

  private final PcService pcService;

  @GetMapping("/getAllPc")
  public ResponseEntity<List<PCDTO>> getAllPc() {
    List<PCDTO> allPc = pcService.getAllPc();
    return ResponseEntity.ok(allPc);
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
