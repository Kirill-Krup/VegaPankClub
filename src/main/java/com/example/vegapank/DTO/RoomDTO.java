package com.example.vegapank.DTO;

import java.util.List;
import lombok.Data;

@Data

public class RoomDTO {
  private Long roomId;
  private String name;
  private Boolean isVip;
  private List<PCDTO> pcsDTO;
}
