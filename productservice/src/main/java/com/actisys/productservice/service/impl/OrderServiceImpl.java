package com.actisys.productservice.service.impl;

import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.dto.Status;
import com.actisys.productservice.exception.OrderNotFoundException;
import com.actisys.productservice.mapper.OrderMapper;
import com.actisys.productservice.model.Order;
import com.actisys.productservice.repository.OrderRepository;
import com.actisys.productservice.service.OrderService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "orders")
public class OrderServiceImpl implements OrderService {

  private final OrderMapper orderMapper;
  private final OrderRepository orderRepository;

  public OrderServiceImpl(OrderMapper orderMapper, OrderRepository orderRepository) {
    this.orderMapper = orderMapper;
    this.orderRepository = orderRepository;
  }

  @Override
  @CacheEvict(value = "orders", allEntries = true)
  public OrderDTO createOrder(OrderDTO order) {
    Order orderToSave = orderMapper.toEntity(order);
    orderRepository.save(orderToSave);
    return orderMapper.toDto(orderToSave);
  }

  @Override
  @Cacheable(value = "orders", key = "#id")
  public OrderDTO getOrderById(Long id) {
    if(!orderRepository.existsById(id)) {
      throw new OrderNotFoundException(id);
    }
    return orderMapper.toDto(orderRepository.getOrderByOrderId((id)));
  }

  @Override
  @Cacheable(value = "orders", key = "'all'")
  public List<OrderDTO> getAllOrders() {
    return orderRepository.findAll().stream().map(orderMapper::toDto).collect(Collectors.toList());
  }

  @Override
  @Cacheable(value = "orders", key = "'status_' + #status.name()")
  public List<OrderDTO> getOrdersByStatus(Status status) {
    return orderRepository.getOrdersByStatus(status).stream().map(orderMapper::toDto).collect(Collectors.toList());
  }

  @Override
  @CacheEvict(allEntries = true)
  public void deleteOrder(Long id) {
    if(!orderRepository.existsById(id)) {
      throw new OrderNotFoundException(id);
    }
    orderRepository.deleteById(id);
  }

  @Override
  @CacheEvict(value = "orders", allEntries = true)
  public OrderDTO updateOrderStatus(Long orderId, Status newStatus) {
    if(!orderRepository.existsById(orderId)) {
      throw new OrderNotFoundException(orderId);
    }
    Order order = orderRepository.getOrderByOrderId(orderId);
    order.setStatus(newStatus);
    return orderMapper.toDto(orderRepository.save(order));
  }
}
