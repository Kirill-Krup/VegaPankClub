package com.actisys.productservice.service;

import com.actisys.common.events.payment.CreatePaymentEvent;
import com.actisys.productservice.dto.OrderDtos.CreateOrderDTO;
import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.dto.Status;
import java.util.List;

public interface OrderService {

  /**
   * Creates new order for specified user with initial CREATED status.
   * Saves order items and total cost, publishes CreateOrderEvent to Kafka for payment processing.
   *
   * @param order DTO with order items and total cost
   * @param userId ID of user placing the order
   * @return DTO of created order with generated ID
   */
  OrderDTO createOrder(CreateOrderDTO order, Long userId);

  /**
   * Retrieves single order by its unique identifier.
   * Returns cached result if available, throws exception if order not found.
   *
   * @param id order identifier
   * @return OrderDTO with full order details
   */
  OrderDTO getOrderById(Long id);

  /**
   * Returns list of all orders in the system.
   * Uses cache with "all" key for performance optimization.
   *
   * @return list of all orders as DTOs
   */
  List<OrderDTO> getAllOrders();

  /**
   * Filters orders by specific status (CREATED, PAID, ERROR_IN_PAID).
   * Results cached using status name as key prefix for efficient filtering.
   *
   * @param status order status to filter by
   * @return list of orders matching the status
   */
  List<OrderDTO> getOrdersByStatus(Status status);

  /**
   * Permanently deletes order by identifier.
   * Clears all orders cache entries after deletion for consistency.
   *
   * @param id order identifier to delete
   */
  void deleteOrder(Long id);

  /**
   * Updates order status to new value (typically PAID or ERROR_IN_PAID).
   * Invalidates all orders cache to reflect status change immediately.
   *
   * @param orderId identifier of order to update
   * @param newStatus new status value
   * @return updated OrderDTO with new status
   */
  OrderDTO updateOrderStatus(Long orderId, Status newStatus);

  /**
   * Updates order status and payment ID based on incoming payment event from Kafka.
   * Maps OperationType.SUCCESS to Status.PAID, others to Status.ERROR_IN_PAID.
   *
   * @param event payment event containing orderId, paymentId and status
   */
  void updateStatusByEvent(CreatePaymentEvent event);
}
