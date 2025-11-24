package com.actisys.productservice.service;

import com.actisys.common.events.payment.CreatePaymentEvent;
import com.actisys.productservice.dto.OrderDtos.CreateOrderDTO;
import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.dto.Status;
import java.util.List;

public interface OrderService {

  OrderDTO createOrder(CreateOrderDTO order, Long userId);

  OrderDTO getOrderById(Long id);

  List<OrderDTO> getAllOrders();

  List<OrderDTO> getOrdersByStatus(Status status);

  void deleteOrder(Long id);

  OrderDTO updateOrderStatus(Long orderId, Status newStatus);

  void updateStatusByEvent(CreatePaymentEvent event);
}
