package com.actisys.inventoryservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class PcUpdateDTO {
  private final String name;
  private final Long roomId;
  private final String cpu;
  private final String gpu;
  private final String ram;
  private final String monitor;
  private final Boolean isEnabled;
  private final Boolean isOccupied;
}
