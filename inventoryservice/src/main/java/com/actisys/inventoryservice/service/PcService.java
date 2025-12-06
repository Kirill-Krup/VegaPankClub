package com.actisys.inventoryservice.service;

import com.actisys.common.clientDtos.PcResponseDTO;
import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
import com.actisys.inventoryservice.dto.PcInfoDTO;
import com.actisys.inventoryservice.dto.PcUpdateDTO;
import java.util.List;

public interface PcService {

  /**
   * Retrieves all PCs with detailed information for admin panel.
   *
   * @return list of all PCs as detailed info DTOs
   */
  List<PcInfoDTO> getAllPc();

  /**
   * Creates new PC record with basic information from DTO.
   *
   * @param pcCreateDTO PC creation data
   * @return created PC as DTO
   */
  PCDTO addNewPc(PcCreateDTO pcCreateDTO);

  /**
   * Updates existing PC specifications, room assignment and status flags.
   * Validates room existence before updating room reference.
   *
   * @param id PC identifier to update
   * @param pcUpdateDTO updated PC specifications and status
   * @return updated PC as DTO
   */
  PCDTO updatePc(Long id, PcUpdateDTO pcUpdateDTO);

  /**
   * Permanently deletes PC record by identifier.
   *
   * @param id PC identifier to delete
   */
  void deletePc(Long id);

  /**
   * Retrieves detailed PC information by identifier for client responses.
   *
   * @param id PC identifier
   * @return PC response DTO for external clients
   */
  PcResponseDTO getPcInfoById(Long id);

  /**
   * Batch retrieves multiple PCs by their identifiers.
   *
   * @param ids list of PC identifiers
   * @return list of PC response DTOs
   */
  List<PcResponseDTO> getPcsByIds(List<Long> ids);

  /**
   * Disables PC by setting enabled flag to false.
   *
   * @param id PC identifier to disable
   * @return updated PC DTO with disabled status
   */
  PCDTO disablePs(Long id);

  /**
   * Activates PC by setting enabled flag to true.
   *
   * @param id PC identifier to activate
   * @return updated PC DTO with active status
   */
  PCDTO activatePs(Long id);
}
