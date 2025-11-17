package com.actisys.inventoryservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PcInfoDTO {
  private final Long id;

  @NotBlank(message = "PC name cannot be blank")
  @Size(max = 100, message = "PC name must be less than 100 characters")
  private final String name;

  @NotBlank(message = "CPU cannot be blank")
  @Size(max = 100)
  private final String cpu;

  @NotBlank(message = "GPU cannot be blank")
  @Size(max = 100)
  private final String gpu;

  @NotBlank(message = "RAM cannot be blank")
  @Size(max = 100)
  private final String ram;

  @NotBlank(message = "Monitor cannot be blank")
  @Size(max = 100)
  private final String monitor;

  @JsonProperty("isEnabled")
  private final boolean isEnabled;
  
  @JsonProperty("isOccupied")
  private final boolean isOccupied;

  @NotNull(message = "Room ID cannot be null")
  private final RoomDTO room;
}
