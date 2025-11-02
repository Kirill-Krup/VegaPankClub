package com.actisys.productservice.repository;

import com.actisys.productservice.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  boolean existsByName(String name);

  @Query("SELECT p FROM Product p JOIN FETCH p.category")
  List<Product> findAllWithCategory();

  @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
  Optional<Product> findByIdWithCategory(@Param("id") Long id);
}
