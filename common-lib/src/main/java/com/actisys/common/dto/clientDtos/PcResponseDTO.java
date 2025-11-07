package com.actisys.common.dto.clientDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class need to share data
 * about pc for SessionResponse
 * */


@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PcResponseDTO {
  private final Long id;
  private final String name;
  private final String roomName;
  private final String cpu;
  private final String gpu;
  private final String ram;
  private final String monitor;
}
