package com.actisys.billingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tariff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "tariff_id")
  private Long tariffId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerHour;

  @Column(name = "is_vip", nullable = false)
  private Boolean isVip = false;

  @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Session> sessions = new ArrayList<>();
}