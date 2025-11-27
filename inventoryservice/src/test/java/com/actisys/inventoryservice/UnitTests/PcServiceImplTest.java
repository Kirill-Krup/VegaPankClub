package com.actisys.inventoryservice.UnitTests;

import com.actisys.common.clientDtos.PcResponseDTO;
import com.actisys.inventoryservice.dto.PCDTO;
import com.actisys.inventoryservice.dto.PcCreateDTO;
import com.actisys.inventoryservice.dto.PcInfoDTO;
import com.actisys.inventoryservice.dto.PcUpdateDTO;
import com.actisys.inventoryservice.dto.RoomDTO;
import com.actisys.inventoryservice.exception.PcNotFoundException;
import com.actisys.inventoryservice.exception.RoomNotFoundException;
import com.actisys.inventoryservice.mapper.PcMapper;
import com.actisys.inventoryservice.model.PC;
import com.actisys.inventoryservice.model.Room;
import com.actisys.inventoryservice.repository.PcRepository;
import com.actisys.inventoryservice.repository.RoomRepository;
import com.actisys.inventoryservice.service.impl.PcServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PcServiceImplTest {

  @Mock
  private PcRepository pcRepository;

  @Mock
  private PcMapper pcMapper;

  @Mock
  private RoomRepository roomRepository;

  @InjectMocks
  private PcServiceImpl pcService;

  private PC testPc;
  private Room testRoom;
  private PCDTO testPcDto;
  private PcInfoDTO testPcInfoDto;
  private PcResponseDTO testPcResponseDto;
  private RoomDTO testRoomDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    testRoom = new Room();
    testRoom.setId(1L);
    testRoom.setName("Room A");

    testRoomDto = RoomDTO.builder()
        .id(1L)
        .name("Room A")
        .build();

    testPc = new PC();
    testPc.setId(1L);
    testPc.setName("PC-01");
    testPc.setRoom(testRoom);
    testPc.setCpu("Intel i7");
    testPc.setGpu("RTX 3060");
    testPc.setRam("16GB");
    testPc.setMonitor("24 inch");
    testPc.setOccupied(false);
    testPc.setEnabled(true);

    testPcDto = PCDTO.builder()
        .id(1L)
        .name("PC-01")
        .build();

    testPcInfoDto = PcInfoDTO.builder()
        .id(1L)
        .name("PC-01")
        .cpu("Intel i7")
        .gpu("RTX 3060")
        .ram("16GB")
        .monitor("24 inch")
        .isEnabled(true)
        .isOccupied(false)
        .room(testRoomDto)
        .build();

    testPcResponseDto = PcResponseDTO.builder()
        .id(1L)
        .name("PC-01")
        .roomName("Room A")
        .cpu("Intel i7")
        .gpu("RTX 3060")
        .ram("16GB")
        .monitor("24 inch")
        .build();
  }

  // ---------- getAllPc ----------

  @Test
  void getAllPc_shouldReturnListOfPcInfoDTOs() {
    // given
    PC pc2 = new PC();
    pc2.setId(2L);
    pc2.setName("PC-02");
    pc2.setRoom(testRoom);
    pc2.setCpu("Intel i5");
    pc2.setGpu("GTX 1660");
    pc2.setRam("8GB");
    pc2.setMonitor("22 inch");
    pc2.setEnabled(true);
    pc2.setOccupied(false);

    PcInfoDTO infoDto2 = PcInfoDTO.builder()
        .id(2L)
        .name("PC-02")
        .cpu("Intel i5")
        .gpu("GTX 1660")
        .ram("8GB")
        .monitor("22 inch")
        .isEnabled(true)
        .isOccupied(false)
        .room(testRoomDto)
        .build();

    when(pcRepository.findAll()).thenReturn(Arrays.asList(testPc, pc2));
    when(pcMapper.toInfoDTO(testPc)).thenReturn(testPcInfoDto);
    when(pcMapper.toInfoDTO(pc2)).thenReturn(infoDto2);

    // when
    List<PcInfoDTO> result = pcService.getAllPc();

    // then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("PC-01", result.get(0).getName());
    assertEquals("Intel i7", result.get(0).getCpu());
    assertEquals("PC-02", result.get(1).getName());
    assertEquals("Intel i5", result.get(1).getCpu());
    verify(pcRepository).findAll();
    verify(pcMapper, times(2)).toInfoDTO(any(PC.class));
  }

  @Test
  void getAllPc_whenNoPcsExist_shouldReturnEmptyList() {
    // given
    when(pcRepository.findAll()).thenReturn(Arrays.asList());

    // when
    List<PcInfoDTO> result = pcService.getAllPc();

    // then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(pcRepository).findAll();
  }

  // ---------- addNewPc ----------

  @Test
  void addNewPc_shouldCreateAndReturnPCDTO() {
    // given
    PcCreateDTO createDto = PcCreateDTO.builder()
        .name("PC-01")
        .roomId(1L)
        .cpu("Intel i7")
        .gpu("RTX 3060")
        .ram("16GB")
        .monitor("24 inch")
        .build();

    when(pcMapper.toEntity(createDto)).thenReturn(testPc);
    when(pcRepository.save(testPc)).thenReturn(testPc);
    when(pcMapper.toDTO(testPc)).thenReturn(testPcDto);

    // when
    PCDTO result = pcService.addNewPc(createDto);

    // then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("PC-01", result.getName());
    verify(pcMapper).toEntity(createDto);
    verify(pcRepository).save(testPc);
    verify(pcMapper).toDTO(testPc);
  }

  // ---------- updatePc ----------

  @Test
  void updatePc_shouldUpdateAndReturnPCDTO() {
    // given
    PcUpdateDTO updateDto = PcUpdateDTO.builder()
        .name("PC-01-Updated")
        .roomId(1L)
        .cpu("Intel i9")
        .gpu("RTX 4090")
        .ram("32GB")
        .monitor("27 inch")
        .isOccupied(true)
        .isEnabled(false)
        .build();

    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(pcRepository.findById(1L)).thenReturn(Optional.of(testPc));
    when(pcRepository.save(testPc)).thenReturn(testPc);
    when(pcMapper.toDTO(testPc)).thenReturn(testPcDto);

    // when
    PCDTO result = pcService.updatePc(1L, updateDto);

    // then
    assertNotNull(result);
    assertEquals("PC-01-Updated", testPc.getName());
    assertEquals("Intel i9", testPc.getCpu());
    assertEquals("RTX 4090", testPc.getGpu());
    assertEquals("32GB", testPc.getRam());
    assertEquals("27 inch", testPc.getMonitor());
    assertTrue(testPc.isOccupied());
    assertFalse(testPc.isEnabled());
    verify(roomRepository).findById(1L);
    verify(pcRepository).findById(1L);
    verify(pcRepository).save(testPc);
  }

  @Test
  void updatePc_whenPcNotFound_shouldThrowException() {
    // given
    PcUpdateDTO updateDto = PcUpdateDTO.builder()
        .name("PC-01")
        .roomId(1L)
        .cpu("Intel i7")
        .gpu("RTX 3060")
        .ram("16GB")
        .monitor("24 inch")
        .isEnabled(true)
        .isOccupied(false)
        .build();

    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(pcRepository.findById(99L)).thenReturn(Optional.empty());

    // when / then
    assertThrows(PcNotFoundException.class,
        () -> pcService.updatePc(99L, updateDto));
    verify(pcRepository, never()).save(any());
  }

  @Test
  void updatePc_whenRoomNotFound_shouldThrowException() {
    // given
    PcUpdateDTO updateDto = PcUpdateDTO.builder()
        .name("PC-01")
        .roomId(99L)
        .cpu("Intel i7")
        .gpu("RTX 3060")
        .ram("16GB")
        .monitor("24 inch")
        .isEnabled(true)
        .isOccupied(false)
        .build();

    when(roomRepository.findById(99L)).thenReturn(Optional.empty());

    // when / then
    assertThrows(RoomNotFoundException.class,
        () -> pcService.updatePc(1L, updateDto));
    verify(pcRepository, never()).findById(any());
    verify(pcRepository, never()).save(any());
  }

  // ---------- deletePc ----------

  @Test
  void deletePc_whenPcExists_shouldDeleteSuccessfully() {
    // given
    when(pcRepository.existsById(1L)).thenReturn(true);

    // when
    pcService.deletePc(1L);

    // then
    verify(pcRepository).existsById(1L);
    verify(pcRepository).deleteById(1L);
  }

  @Test
  void deletePc_whenPcNotFound_shouldThrowException() {
    // given
    when(pcRepository.existsById(99L)).thenReturn(false);

    // when / then
    assertThrows(PcNotFoundException.class,
        () -> pcService.deletePc(99L));
    verify(pcRepository).existsById(99L);
    verify(pcRepository, never()).deleteById(any());
  }

  // ---------- getPcInfoById ----------

  @Test
  void getPcInfoById_shouldReturnPcResponseDTO() {
    // given
    when(pcRepository.findById(1L)).thenReturn(Optional.of(testPc));
    when(pcMapper.toResponseDto(testPc)).thenReturn(testPcResponseDto);

    // when
    PcResponseDTO result = pcService.getPcInfoById(1L);

    // then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("PC-01", result.getName());
    assertEquals("Room A", result.getRoomName());
    assertEquals("Intel i7", result.getCpu());
    assertEquals("RTX 3060", result.getGpu());
    assertEquals("16GB", result.getRam());
    assertEquals("24 inch", result.getMonitor());
    verify(pcRepository).findById(1L);
    verify(pcMapper).toResponseDto(testPc);
  }

  @Test
  void getPcInfoById_whenPcNotFound_shouldThrowException() {
    // given
    when(pcRepository.findById(99L)).thenReturn(Optional.empty());

    // when / then
    assertThrows(PcNotFoundException.class,
        () -> pcService.getPcInfoById(99L));
    verify(pcRepository).findById(99L);
  }

  // ---------- getPcsByIds ----------

  @Test
  void getPcsByIds_shouldReturnListOfPcResponseDTOs() {
    // given
    PC pc2 = new PC();
    pc2.setId(2L);
    pc2.setName("PC-02");
    pc2.setRoom(testRoom);
    pc2.setCpu("Intel i5");
    pc2.setGpu("GTX 1660");
    pc2.setRam("8GB");
    pc2.setMonitor("22 inch");

    PcResponseDTO responseDto2 = PcResponseDTO.builder()
        .id(2L)
        .name("PC-02")
        .roomName("Room A")
        .cpu("Intel i5")
        .gpu("GTX 1660")
        .ram("8GB")
        .monitor("22 inch")
        .build();

    List<Long> ids = Arrays.asList(1L, 2L);
    when(pcRepository.findAllByIdIn(ids)).thenReturn(Arrays.asList(testPc, pc2));
    when(pcMapper.toResponseDto(testPc)).thenReturn(testPcResponseDto);
    when(pcMapper.toResponseDto(pc2)).thenReturn(responseDto2);

    // when
    List<PcResponseDTO> result = pcService.getPcsByIds(ids);

    // then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("PC-01", result.get(0).getName());
    assertEquals("Intel i7", result.get(0).getCpu());
    assertEquals("PC-02", result.get(1).getName());
    assertEquals("Intel i5", result.get(1).getCpu());
    verify(pcRepository).findAllByIdIn(ids);
    verify(pcMapper, times(2)).toResponseDto(any(PC.class));
  }

  @Test
  void getPcsByIds_whenNoPcsFound_shouldReturnEmptyList() {
    // given
    List<Long> ids = Arrays.asList(99L, 100L);
    when(pcRepository.findAllByIdIn(ids)).thenReturn(Arrays.asList());

    // when
    List<PcResponseDTO> result = pcService.getPcsByIds(ids);

    // then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(pcRepository).findAllByIdIn(ids);
  }

  // ---------- disablePs ----------

  @Test
  void disablePs_shouldSetEnabledToFalse() {
    // given
    testPc.setEnabled(true);
    when(pcRepository.findById(1L)).thenReturn(Optional.of(testPc));
    when(pcRepository.save(testPc)).thenReturn(testPc);
    when(pcMapper.toDTO(testPc)).thenReturn(testPcDto);

    // when
    PCDTO result = pcService.disablePs(1L);

    // then
    assertNotNull(result);
    assertFalse(testPc.isEnabled());
    verify(pcRepository).findById(1L);
    verify(pcRepository).save(testPc);
    verify(pcMapper).toDTO(testPc);
  }

  @Test
  void disablePs_whenPcNotFound_shouldThrowException() {
    // given
    when(pcRepository.findById(99L)).thenReturn(Optional.empty());

    // when / then
    assertThrows(PcNotFoundException.class,
        () -> pcService.disablePs(99L));
    verify(pcRepository, never()).save(any());
  }

  // ---------- activatePs ----------

  @Test
  void activatePs_shouldSetEnabledToTrue() {
    // given
    testPc.setEnabled(false);
    when(pcRepository.findById(1L)).thenReturn(Optional.of(testPc));
    when(pcRepository.save(testPc)).thenReturn(testPc);
    when(pcMapper.toDTO(testPc)).thenReturn(testPcDto);

    // when
    PCDTO result = pcService.activatePs(1L);

    // then
    assertNotNull(result);
    assertTrue(testPc.isEnabled());
    verify(pcRepository).findById(1L);
    verify(pcRepository).save(testPc);
    verify(pcMapper).toDTO(testPc);
  }

  @Test
  void activatePs_whenPcNotFound_shouldThrowException() {
    // given
    when(pcRepository.findById(99L)).thenReturn(Optional.empty());

    // when / then
    assertThrows(PcNotFoundException.class,
        () -> pcService.activatePs(99L));
    verify(pcRepository, never()).save(any());
  }
}
