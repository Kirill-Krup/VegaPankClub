package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.PC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PcRepository extends JpaRepository<PC, Long> {

}
