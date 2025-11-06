package com.actisys.apigatewayapplication.dto.AdminSessionDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

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

  private final List<PCDTO> pcs;
}
