package com.actisys.inventoryservice.service;

import com.actisys.common.clientDtos.PcResponseDTO;
import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
import com.actisys.inventoryservice.dto.PcInfoDTO;
import com.actisys.inventoryservice.dto.PcUpdateDTO;
import java.util.List;

public interface PcService {

  List<PcInfoDTO> getAllPc();

  PCDTO addNewPc(PcCreateDTO pcCreateDTO);

  PCDTO updatePc(Long id, PcUpdateDTO pcUpdateDTO);

  void deletePc(Long id);

  PcResponseDTO getPcInfoById(Long id);

  List<PcResponseDTO> getPcsByIds(List<Long> ids);

  PCDTO disablePs(Long id);

  PCDTO activatePs(Long id);
}
