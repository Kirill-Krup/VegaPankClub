package com.actisys.billingservice.repository;

import com.actisys.billingservice.model.Session;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

  List<Session> findAllByUserIdAndEndTimeIsNotNull(Long userId);
}
