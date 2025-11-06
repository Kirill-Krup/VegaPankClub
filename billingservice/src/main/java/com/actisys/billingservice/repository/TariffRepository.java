package com.actisys.billingservice.repository;

import com.actisys.billingservice.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffRepository extends JpaRepository<Tariff,Long> {

  boolean existsByName(String name);
}
