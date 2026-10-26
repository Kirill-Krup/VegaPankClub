package com.actisys.userservice.repository;

import com.actisys.userservice.model.WalletOperation;
import com.actisys.userservice.model.WalletProcessedEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletProcessedEventRepository extends JpaRepository<WalletProcessedEvent, Long> {

  Optional<WalletProcessedEvent> findByPaymentIdAndOperation(Long paymentId, WalletOperation operation);

  boolean existsByPaymentIdAndOperation(Long paymentId, WalletOperation operation);
}
