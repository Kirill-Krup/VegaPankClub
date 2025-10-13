package com.actisys.inventoryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class PCDTO {

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

  private final boolean isEnabled;
  private final boolean isOccupied;

  @NotNull(message = "Room ID cannot be null")
  private final Long roomId;
}
