package com.example.vegapank.DTO;

import java.util.List;
import lombok.Data;

@Data
public class PCDTO {
  private Long pcId;
  private String name;
  private String cpu;
  private String gpu;
  private String ram;
  private String monitor;
  private Boolean isEnabled;
  private RoomDTO roomDTO;
  private List<SessionDTO> sessions;
}
