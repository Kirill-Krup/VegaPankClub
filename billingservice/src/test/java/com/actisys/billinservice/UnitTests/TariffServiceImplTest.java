package com.actisys.billinservice.UnitTests;

import com.actisys.billingservice.dto.TariffDtos.CreateTariffDTO;
import com.actisys.billingservice.dto.TariffDtos.TariffDTO;
import com.actisys.billingservice.exception.TariffAlreadyExistsException;
import com.actisys.billingservice.exception.TariffNotFoundException;
import com.actisys.billingservice.mapper.TariffMapper;
import com.actisys.billingservice.model.Tariff;
import com.actisys.billingservice.repository.TariffRepository;
import com.actisys.billingservice.service.impl.TariffServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Limit;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TariffServiceImplTest {

  @Mock
  private TariffRepository tariffRepository;

  @Mock
  private TariffMapper tariffMapper;

  @InjectMocks
  private TariffServiceImpl tariffService;

  private Tariff testTariff;
  private TariffDTO testTariffDto;
  private CreateTariffDTO createTariffDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    testTariff = Tariff.builder()
        .tariffId(1L)
        .name("Standard")
        .price(BigDecimal.valueOf(100))
        .isVip(false)
        .hours(1)
        .build();

    testTariffDto = TariffDTO.builder()
        .tariffId(1L)
        .name("Standard")
        .price(BigDecimal.valueOf(100))
        .isVip(false)
        .hours(1)
        .build();

    createTariffDto = CreateTariffDTO.builder()
        .name("Standard")
        .price(BigDecimal.valueOf(100))
        .isVip(false)
        .hours(1)
        .build();
  }

  @Test
  void getAllTariffs_shouldReturnListOfTariffDTOs() {
    Tariff tariff2 = Tariff.builder()
        .tariffId(2L)
        .name("Premium")
        .price(BigDecimal.valueOf(200))
        .isVip(true)
        .hours(2)
        .build();

    TariffDTO dto2 = TariffDTO.builder()
        .tariffId(2L)
        .name("Premium")
        .price(BigDecimal.valueOf(200))
        .isVip(true)
        .hours(2)
        .build();

    when(tariffRepository.findAll()).thenReturn(Arrays.asList(testTariff, tariff2));
    when(tariffMapper.toDTO(testTariff)).thenReturn(testTariffDto);
    when(tariffMapper.toDTO(tariff2)).thenReturn(dto2);

    List<TariffDTO> result = tariffService.getAllTariffs();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Standard", result.get(0).getName());
    assertEquals(BigDecimal.valueOf(100), result.get(0).getPrice());
    assertEquals("Premium", result.get(1).getName());
    assertEquals(BigDecimal.valueOf(200), result.get(1).getPrice());
    verify(tariffRepository).findAll();
    verify(tariffMapper, times(2)).toDTO(any(Tariff.class));
  }

  @Test
  void getAllTariffs_whenNoTariffsExist_shouldReturnEmptyList() {
    when(tariffRepository.findAll()).thenReturn(Collections.emptyList());

    List<TariffDTO> result = tariffService.getAllTariffs();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tariffRepository).findAll();
  }

  @Test
  void createTariff_shouldCreateAndReturnTariffDTO() {
    when(tariffRepository.existsByName("Standard")).thenReturn(false);
    when(tariffRepository.save(any(Tariff.class))).thenReturn(testTariff);
    when(tariffMapper.toDTO(testTariff)).thenReturn(testTariffDto);

    TariffDTO result = tariffService.createTariff(createTariffDto);

    assertNotNull(result);
    assertEquals("Standard", result.getName());
    assertEquals(BigDecimal.valueOf(100), result.getPrice());
    assertFalse(result.isVip());
    assertEquals(1, result.getHours());
    verify(tariffRepository).existsByName("Standard");
    verify(tariffRepository).save(any(Tariff.class));
    verify(tariffMapper).toDTO(testTariff);
  }

  @Test
  void createTariff_whenNameAlreadyExists_shouldThrowException() {
    when(tariffRepository.existsByName("Standard")).thenReturn(true);

    assertThrows(TariffAlreadyExistsException.class,
        () -> tariffService.createTariff(createTariffDto));
    verify(tariffRepository).existsByName("Standard");
    verify(tariffRepository, never()).save(any());
  }

  @Test
  void createTariff_withVipTariff_shouldCreateSuccessfully() {
    CreateTariffDTO vipDto = CreateTariffDTO.builder()
        .name("VIP Gold")
        .price(BigDecimal.valueOf(500))
        .isVip(true)
        .hours(5)
        .build();

    Tariff vipTariff = Tariff.builder()
        .tariffId(3L)
        .name("VIP Gold")
        .price(BigDecimal.valueOf(500))
        .isVip(true)
        .hours(5)
        .build();

    TariffDTO vipTariffDto = TariffDTO.builder()
        .tariffId(3L)
        .name("VIP Gold")
        .price(BigDecimal.valueOf(500))
        .isVip(true)
        .hours(5)
        .build();

    when(tariffRepository.existsByName("VIP Gold")).thenReturn(false);
    when(tariffRepository.save(any(Tariff.class))).thenReturn(vipTariff);
    when(tariffMapper.toDTO(vipTariff)).thenReturn(vipTariffDto);

    TariffDTO result = tariffService.createTariff(vipDto);

    assertNotNull(result);
    assertTrue(result.isVip());
    assertEquals(BigDecimal.valueOf(500), result.getPrice());
    verify(tariffRepository).save(any(Tariff.class));
  }

  @Test
  void updateTariff_shouldUpdateAndReturnTariffDTO() {
    CreateTariffDTO updateDto = CreateTariffDTO.builder()
        .name("Standard Updated")
        .price(BigDecimal.valueOf(150))
        .isVip(false)
        .hours(2)
        .build();

    TariffDTO updatedDto = TariffDTO.builder()
        .tariffId(1L)
        .name("Standard Updated")
        .price(BigDecimal.valueOf(150))
        .isVip(false)
        .hours(2)
        .build();

    when(tariffRepository.findById(1L)).thenReturn(Optional.of(testTariff));
    when(tariffRepository.existsByName("Standard Updated")).thenReturn(false);
    when(tariffRepository.save(testTariff)).thenReturn(testTariff);
    when(tariffMapper.toDTO(testTariff)).thenReturn(updatedDto);

    TariffDTO result = tariffService.updateTariff(1L, updateDto);

    assertNotNull(result);
    assertEquals("Standard Updated", result.getName());
    assertEquals(BigDecimal.valueOf(150), result.getPrice());
    assertEquals(2, result.getHours());
    verify(tariffRepository).findById(1L);
    verify(tariffRepository).existsByName("Standard Updated");
    verify(tariffRepository).save(testTariff);
  }

  @Test
  void updateTariff_whenSameName_shouldNotCheckExistence() {
    CreateTariffDTO updateDto = CreateTariffDTO.builder()
        .name("Standard")
        .price(BigDecimal.valueOf(150))
        .isVip(false)
        .hours(2)
        .build();

    when(tariffRepository.findById(1L)).thenReturn(Optional.of(testTariff));
    when(tariffRepository.save(testTariff)).thenReturn(testTariff);
    when(tariffMapper.toDTO(testTariff)).thenReturn(testTariffDto);

    TariffDTO result = tariffService.updateTariff(1L, updateDto);

    assertNotNull(result);
    verify(tariffRepository).findById(1L);
    verify(tariffRepository, never()).existsByName(anyString());
    verify(tariffRepository).save(testTariff);
  }

  @Test
  void updateTariff_whenTariffNotFound_shouldThrowException() {
    when(tariffRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(TariffNotFoundException.class,
        () -> tariffService.updateTariff(99L, createTariffDto));
    verify(tariffRepository).findById(99L);
    verify(tariffRepository, never()).save(any());
  }

  @Test
  void updateTariff_whenNewNameAlreadyExists_shouldThrowException() {
    CreateTariffDTO updateDto = CreateTariffDTO.builder()
        .name("Premium")
        .price(BigDecimal.valueOf(150))
        .isVip(false)
        .hours(2)
        .build();

    when(tariffRepository.findById(1L)).thenReturn(Optional.of(testTariff));
    when(tariffRepository.existsByName("Premium")).thenReturn(true);

    assertThrows(TariffAlreadyExistsException.class,
        () -> tariffService.updateTariff(1L, updateDto));
    verify(tariffRepository).findById(1L);
    verify(tariffRepository).existsByName("Premium");
    verify(tariffRepository, never()).save(any());
  }

  @Test
  void deleteTariff_whenTariffExists_shouldDeleteSuccessfully() {
    when(tariffRepository.findById(1L)).thenReturn(Optional.of(testTariff));

    tariffService.deleteTariff(1L);

    verify(tariffRepository).findById(1L);
    verify(tariffRepository).delete(testTariff);
  }

  @Test
  void deleteTariff_whenTariffNotFound_shouldThrowException() {
    when(tariffRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(TariffNotFoundException.class,
        () -> tariffService.deleteTariff(99L));
    verify(tariffRepository).findById(99L);
    verify(tariffRepository, never()).delete(any());
  }

  @Test
  void getPopularTariffs_whenEnoughPopularTariffs_shouldReturnThree() {
    Tariff tariff2 = Tariff.builder()
        .tariffId(2L)
        .name("Premium")
        .price(BigDecimal.valueOf(200))
        .isVip(true)
        .hours(2)
        .build();

    Tariff tariff3 = Tariff.builder()
        .tariffId(3L)
        .name("Basic")
        .price(BigDecimal.valueOf(50))
        .isVip(false)
        .hours(1)
        .build();

    TariffDTO dto2 = TariffDTO.builder()
        .tariffId(2L)
        .name("Premium")
        .price(BigDecimal.valueOf(200))
        .isVip(true)
        .hours(2)
        .build();

    TariffDTO dto3 = TariffDTO.builder()
        .tariffId(3L)
        .name("Basic")
        .price(BigDecimal.valueOf(50))
        .isVip(false)
        .hours(1)
        .build();

    List<Tariff> popularTariffs = Arrays.asList(testTariff, tariff2, tariff3);
    when(tariffRepository.findPopularTariffs(Limit.of(3))).thenReturn(popularTariffs);
    when(tariffMapper.toDTO(testTariff)).thenReturn(testTariffDto);
    when(tariffMapper.toDTO(tariff2)).thenReturn(dto2);
    when(tariffMapper.toDTO(tariff3)).thenReturn(dto3);

    List<TariffDTO> result = tariffService.getPopularTariffs();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("Standard", result.get(0).getName());
    assertEquals("Premium", result.get(1).getName());
    assertEquals("Basic", result.get(2).getName());
    verify(tariffRepository).findPopularTariffs(Limit.of(3));
    verify(tariffRepository, never()).findFirst3By();
    verify(tariffMapper, times(3)).toDTO(any(Tariff.class));
  }

  @Test
  void getPopularTariffs_whenLessThanThreePopular_shouldFillWithFirst3() {
    Tariff popularTariff = Tariff.builder()
        .tariffId(1L)
        .name("Popular")
        .price(BigDecimal.valueOf(100))
        .isVip(false)
        .hours(1)
        .build();

    Tariff additionalTariff1 = Tariff.builder()
        .tariffId(2L)
        .name("Additional1")
        .price(BigDecimal.valueOf(50))
        .isVip(false)
        .hours(1)
        .build();

    Tariff additionalTariff2 = Tariff.builder()
        .tariffId(3L)
        .name("Additional2")
        .price(BigDecimal.valueOf(75))
        .isVip(false)
        .hours(1)
        .build();

    TariffDTO popularDto = TariffDTO.builder()
        .tariffId(1L)
        .name("Popular")
        .price(BigDecimal.valueOf(100))
        .isVip(false)
        .hours(1)
        .build();

    TariffDTO additionalDto1 = TariffDTO.builder()
        .tariffId(2L)
        .name("Additional1")
        .price(BigDecimal.valueOf(50))
        .isVip(false)
        .hours(1)
        .build();

    TariffDTO additionalDto2 = TariffDTO.builder()
        .tariffId(3L)
        .name("Additional2")
        .price(BigDecimal.valueOf(75))
        .isVip(false)
        .hours(1)
        .build();

    when(tariffRepository.findPopularTariffs(Limit.of(3)))
        .thenReturn(Collections.singletonList(popularTariff));
    when(tariffRepository.findFirst3By())
        .thenReturn(Arrays.asList(popularTariff, additionalTariff1, additionalTariff2));
    when(tariffMapper.toDTO(popularTariff)).thenReturn(popularDto);
    when(tariffMapper.toDTO(additionalTariff1)).thenReturn(additionalDto1);
    when(tariffMapper.toDTO(additionalTariff2)).thenReturn(additionalDto2);

    List<TariffDTO> result = tariffService.getPopularTariffs();

    assertNotNull(result);
    assertEquals(3, result.size());
    verify(tariffRepository).findPopularTariffs(Limit.of(3));
    verify(tariffRepository).findFirst3By();
    verify(tariffMapper, times(3)).toDTO(any(Tariff.class));
  }

  @Test
  void getPopularTariffs_whenNoPopularTariffs_shouldReturnFirst3() {
    Tariff tariff1 = Tariff.builder().tariffId(1L).name("T1").price(BigDecimal.valueOf(100)).isVip(false).hours(1).build();
    Tariff tariff2 = Tariff.builder().tariffId(2L).name("T2").price(BigDecimal.valueOf(100)).isVip(false).hours(1).build();
    Tariff tariff3 = Tariff.builder().tariffId(3L).name("T3").price(BigDecimal.valueOf(100)).isVip(false).hours(1).build();

    TariffDTO dto1 = TariffDTO.builder().tariffId(1L).name("T1").price(BigDecimal.valueOf(100)).isVip(false).hours(1).build();
    TariffDTO dto2 = TariffDTO.builder().tariffId(2L).name("T2").price(BigDecimal.valueOf(100)).isVip(false).hours(1).build();
    TariffDTO dto3 = TariffDTO.builder().tariffId(3L).name("T3").price(BigDecimal.valueOf(100)).isVip(false).hours(1).build();

    when(tariffRepository.findPopularTariffs(Limit.of(3))).thenReturn(Collections.emptyList());
    when(tariffRepository.findFirst3By()).thenReturn(Arrays.asList(tariff1, tariff2, tariff3));
    when(tariffMapper.toDTO(tariff1)).thenReturn(dto1);
    when(tariffMapper.toDTO(tariff2)).thenReturn(dto2);
    when(tariffMapper.toDTO(tariff3)).thenReturn(dto3);

    List<TariffDTO> result = tariffService.getPopularTariffs();

    assertNotNull(result);
    assertEquals(3, result.size());
    verify(tariffRepository).findPopularTariffs(Limit.of(3));
    verify(tariffRepository).findFirst3By();
  }

  @Test
  void getPopularTariffs_whenDuplicatesInBothSources_shouldReturnUniqueThree() {
    Tariff popularTariff = Tariff.builder()
        .tariffId(1L)
        .name("Popular")
        .price(BigDecimal.valueOf(100))
        .isVip(false)
        .hours(1)
        .build();

    Tariff additionalTariff1 = Tariff.builder()
        .tariffId(2L)
        .name("Additional1")
        .price(BigDecimal.valueOf(50))
        .isVip(false)
        .hours(1)
        .build();

    Tariff additionalTariff2 = Tariff.builder()
        .tariffId(3L)
        .name("Additional2")
        .price(BigDecimal.valueOf(75))
        .isVip(false)
        .hours(1)
        .build();

    TariffDTO popularDto = TariffDTO.builder()
        .tariffId(1L)
        .name("Popular")
        .price(BigDecimal.valueOf(100))
        .isVip(false)
        .hours(1)
        .build();

    TariffDTO additionalDto1 = TariffDTO.builder()
        .tariffId(2L)
        .name("Additional1")
        .price(BigDecimal.valueOf(50))
        .isVip(false)
        .hours(1)
        .build();

    TariffDTO additionalDto2 = TariffDTO.builder()
        .tariffId(3L)
        .name("Additional2")
        .price(BigDecimal.valueOf(75))
        .isVip(false)
        .hours(1)
        .build();

    when(tariffRepository.findPopularTariffs(Limit.of(3)))
        .thenReturn(Arrays.asList(popularTariff, additionalTariff1));
    when(tariffRepository.findFirst3By())
        .thenReturn(Arrays.asList(popularTariff, additionalTariff1, additionalTariff2));
    when(tariffMapper.toDTO(popularTariff)).thenReturn(popularDto);
    when(tariffMapper.toDTO(additionalTariff1)).thenReturn(additionalDto1);
    when(tariffMapper.toDTO(additionalTariff2)).thenReturn(additionalDto2);

    List<TariffDTO> result = tariffService.getPopularTariffs();

    assertNotNull(result);
    assertEquals(3, result.size());
    verify(tariffRepository).findPopularTariffs(Limit.of(3));
    verify(tariffRepository).findFirst3By();
  }
}
