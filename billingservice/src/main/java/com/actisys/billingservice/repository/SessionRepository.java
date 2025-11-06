package com.actisys.billingservice.repository;

import com.actisys.billingservice.model.Session;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

  List<Session> findAllByUserIdAndEndTimeIsNotNull(Long userId);

  @Query("SELECT s FROM Session s WHERE " +
      "s.startTime < :endOfDay AND s.endTime > :startOfDay")
  List<Session> findSessionsIntersectingDay(
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay
  );
}
