package com.actisys.inventoryservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pc")
@Getter
@Setter
@RequiredArgsConstructor
public class PC {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pc_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @Column(nullable = false)
  private String cpu;

  @Column(nullable = false)
  private String gpu;

  @Column(nullable = false)
  private String ram;

  @Column(nullable = false)
  private String monitor;

  @Column(name = "is_enabled", nullable = false)
  private boolean isEnabled = true;

  @Column(name = "is_occupied", nullable = false)
  private boolean isOccupied = false;

}
