package com.example.vegapank.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pc")
public class PC {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pc_id")
  private Long pcId;

  private String name;

  @Column(name = "cpu")
  private String cpu;

  @Column(name = "gpu")
  private String gpu;

  @Column(name = "ram")
  private String ram;

  private String monitor;

  @Column(name = "is_enabled")
  private Boolean isEnabled;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private Room room;

  @OneToMany(mappedBy = "pc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Session> sessions;
}