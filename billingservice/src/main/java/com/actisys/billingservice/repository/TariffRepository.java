package com.actisys.billingservice.repository;

import com.actisys.billingservice.model.Tariff;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffRepository extends JpaRepository<Tariff,Long> {

  boolean existsByName(String name);

  @Query("SELECT s.tariff FROM Session s " +
      "GROUP BY s.tariff " +
      "ORDER BY COUNT(s.tariff) DESC")
  List<Tariff> findPopularTariffs(Limit limit);

  List<Tariff> findFirst3By();


}
