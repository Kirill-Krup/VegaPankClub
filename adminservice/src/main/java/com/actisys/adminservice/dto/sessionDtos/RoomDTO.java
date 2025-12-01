package com.actisys.adminservice.dto.sessionDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@EqualsAndHashCode
@RequiredArgsConstructor
public class RoomDTO {

  private final Long id;

  @NotBlank(message = "Room name cannot be blank")
  @Size(max = 100, message = "Room name must be less than 100 characters")
  private final String name;

  private final boolean isVip;
}
