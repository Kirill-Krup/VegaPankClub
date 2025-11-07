package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.PC;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PcRepository extends JpaRepository<PC, Long> {

  List<PC> findAllByIdIn(Collection<Long> ids);
}
