package com.actisys.inventoryservice.service.impl;

import com.actisys.common.dto.clientDtos.PcResponseDTO;
import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
import com.actisys.inventoryservice.dto.PcInfoDTO;
import com.actisys.inventoryservice.dto.PcUpdateDTO;
import com.actisys.inventoryservice.exception.PcNotFoundException;
import com.actisys.inventoryservice.exception.RoomNotFoundException;
import com.actisys.inventoryservice.mapper.PcMapper;
import com.actisys.inventoryservice.model.PC;
import com.actisys.inventoryservice.model.Room;
import com.actisys.inventoryservice.repository.PcRepository;
import com.actisys.inventoryservice.repository.RoomRepository;
import com.actisys.inventoryservice.service.PcService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PcServiceImpl implements PcService {

  private final PcRepository pcRepository;
  private final PcMapper pcMapper;
  private final RoomRepository roomRepository;

  @Override
  public List<PcInfoDTO> getAllPc() {
    List<PC> allPc = pcRepository.findAll();
    return allPc.stream().map(pcMapper::toInfoDTO).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public PCDTO addNewPc(PcCreateDTO pcCreateDTO) {
    PC newPc = pcMapper.toEntity(pcCreateDTO);
    return pcMapper.toDTO(pcRepository.save(newPc));
  }

  @Override
  @Transactional
  public PCDTO updatePc(Long id, PcUpdateDTO pcUpdateDTO) {
    Room room = roomRepository.findById(pcUpdateDTO.getRoomId()).orElseThrow(()->
        new RoomNotFoundException(pcUpdateDTO.getRoomId()));
    PC pc = pcRepository.findById(id).orElseThrow(() -> new PcNotFoundException(id));
    pc.setName(pcUpdateDTO.getName());
    pc.setRoom(room);
    pc.setCpu(pcUpdateDTO.getCpu());
    pc.setGpu(pcUpdateDTO.getGpu());
    pc.setRam(pcUpdateDTO.getRam());
    pc.setMonitor(pcUpdateDTO.getMonitor());
    pc.setOccupied(pcUpdateDTO.getIsOccupied());
    pc.setEnabled(pcUpdateDTO.getIsEnabled());
    return pcMapper.toDTO(pcRepository.save(pc));
  }

  @Override
  @Transactional
  public void deletePc(Long id) {
    if(!pcRepository.existsById(id)) {
      throw new PcNotFoundException(id);
    }
    pcRepository.deleteById(id);
  }

  @Override
  public PcResponseDTO getPcInfoById(Long id) {
    PC pc = pcRepository.findById(id).orElseThrow(()->new PcNotFoundException(id));
    return pcMapper.toResponseDto(pc);
  }

  @Override
  public List<PcResponseDTO> getPcsByIds(List<Long> ids) {
    List<PC> pcs = pcRepository.findAllByIdIn(ids);
    return pcs.stream().map(pcMapper::toResponseDto).collect(Collectors.toList());
  }

  @Override
  public PCDTO disablePs(Long id) {
    PC pc = pcRepository.findById(id).orElseThrow(()->new PcNotFoundException(id));
    pc.setEnabled(false);
    return pcMapper.toDTO(pcRepository.save(pc));
  }

  @Override
  public PCDTO activatePs(Long id) {
    PC pc = pcRepository.findById(id).orElseThrow(()->new PcNotFoundException(id));
    pc.setEnabled(true);
    return pcMapper.toDTO(pcRepository.save(pc));
  }
}
