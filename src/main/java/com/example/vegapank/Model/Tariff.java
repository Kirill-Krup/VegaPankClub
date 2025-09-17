package com.example.vegapank.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tariff")
public class Tariff {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "tariff_id")
  private Long tariffId;

  private String name;

  @Column(name = "price_per_hour")
  private Double pricePerHour;

  @Column(name = "is_vip")
  private Boolean isVip;

  @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Session> sessions;
}
