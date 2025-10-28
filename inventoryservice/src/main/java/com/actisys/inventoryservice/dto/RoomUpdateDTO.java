package com.actisys.inventoryservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RoomUpdateDTO {
  private final String name;
  private final boolean isVip;
}
