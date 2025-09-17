package com.example.vegapank.DTO;

import java.time.LocalDateTime;
import lombok.Data;

@Data

public class SessionDTO {
  private Long sessionId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Double totalCost;
  private String status;
  private PCDTO pcDTO;
  private TariffDTO tariffDTO;
}
