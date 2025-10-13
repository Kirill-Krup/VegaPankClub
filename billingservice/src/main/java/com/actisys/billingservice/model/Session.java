package com.actisys.billingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "session_id")
  private Long sessionId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "pc_id", nullable = false)
  private Long pcId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tariff_id", nullable = false)
  private Tariff tariff;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;

  @Column(name = "total_cost", precision = 10, scale = 2)
  private BigDecimal totalCost;

  @Column(name = "status", nullable = false, length = 50)
  private String status;
}