package com.actisys.inventoryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class RoomDTO {

  private final Long id;

  @NotBlank(message = "Room name cannot be blank")
  @Size(max = 100, message = "Room name must be less than 100 characters")
  private final String name;

  private final boolean isVip;
}
