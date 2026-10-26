package com.actisys.userservice.model;

import com.actisys.common.events.OperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "wallet_processed_event",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_wallet_processed_payment_operation",
        columnNames = {"payment_id", "operation"}
    )
)
public class WalletProcessedEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "payment_id", nullable = false)
  private Long paymentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation", nullable = false, length = 32)
  private WalletOperation operation;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private OperationType status;

  @Column(name = "processed_at", nullable = false)
  private LocalDateTime processedAt = LocalDateTime.now();
}
