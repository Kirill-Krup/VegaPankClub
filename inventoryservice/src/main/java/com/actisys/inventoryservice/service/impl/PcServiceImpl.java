package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
import com.actisys.inventoryservice.dto.PcUpdateDTO;
import com.actisys.inventoryservice.exception.PcNotFoundException;
import com.actisys.inventoryservice.mapper.PcMapper;
import com.actisys.inventoryservice.model.PC;
import com.actisys.inventoryservice.repository.PcRepository;
import com.actisys.inventoryservice.service.PcService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PcServiceImpl implements PcService {

  private final PcRepository pcRepository;
  private final PcMapper pcMapper;

  @Override
  public List<PCDTO> getAllPc() {
    List<PC> allPc = pcRepository.findAll();
    return allPc.stream().map(pcMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  public PCDTO addNewPc(PcCreateDTO pcCreateDTO) {
    PC newPc = pcMapper.toEntity(pcCreateDTO);
    return pcMapper.toDTO(pcRepository.save(newPc));
  }

  @Override
  public PCDTO updatePc(Long id, PcUpdateDTO pcUpdateDTO) {
    PC pc = pcRepository.findById(id).orElseThrow(() -> new PcNotFoundException(id));
    pc.setName(pcUpdateDTO.getName());
    pc.setRoomId(pcUpdateDTO.getRoomId());
    pc.setCpu(pcUpdateDTO.getCpu());
    pc.setGpu(pcUpdateDTO.getGpu());
    pc.setRam(pcUpdateDTO.getRam());
    pc.setMonitor(pcUpdateDTO.getMonitor());
    pc.setOccupied(pcUpdateDTO.getIsOccupied());
    pc.setEnabled(pcUpdateDTO.getIsEnabled());
    return pcMapper.toDTO(pcRepository.save(pc));
  }

  @Override
  public void deletePc(Long id) {
    if(!pcRepository.existsById(id)) {
      throw new PcNotFoundException(id);
    }
    pcRepository.deleteById(id);
  }
}
