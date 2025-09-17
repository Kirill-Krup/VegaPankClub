package com.example.vegapank.DTO;

import java.util.List;
import lombok.Data;

@Data

public class TariffDTO {
  private Long tariffId;
  private String name;
  private Double pricePerHour;
  private Boolean isVip;
  private List<SessionDTO> sessionsDTO;
}
