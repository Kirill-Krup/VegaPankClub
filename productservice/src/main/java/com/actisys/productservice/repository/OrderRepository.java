package com.actisys.productservice.repository;

import com.actisys.productservice.dto.OrderDTO;
import com.actisys.productservice.dto.Status;
import com.actisys.productservice.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  Order getOrderByOrderId(Long orderId);

  List<Order> getOrdersByStatus(Status status);

}
