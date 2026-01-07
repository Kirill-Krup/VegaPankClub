package com.actisys.productservice.service.impl;

import com.actisys.common.events.OperationType;
import com.actisys.common.events.PaymentType;
import com.actisys.common.events.order.CreateOrderEvent;
import com.actisys.common.events.payment.CreatePaymentEvent;
import com.actisys.productservice.dto.OrderDtos.CreateOrderDTO;
import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.dto.OrderDtos.OrderItemDTO;
import com.actisys.productservice.dto.Status;
import com.actisys.productservice.exception.OrderNotFoundException;
import com.actisys.productservice.exception.ProductNotFoundException;
import com.actisys.productservice.mapper.OrderItemMapper;
import com.actisys.productservice.mapper.OrderMapper;
import com.actisys.productservice.model.Order;
import com.actisys.productservice.model.OrderItem;
import com.actisys.productservice.model.Product;
import com.actisys.productservice.repository.OrderRepository;
import com.actisys.productservice.repository.ProductRepository;
import com.actisys.productservice.service.OrderService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CacheConfig(cacheNames = "orders")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderMapper orderMapper;
  private final OrderRepository orderRepository;

  private final ProductRepository productRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  @CacheEvict(value = "orders", allEntries = true)
  public OrderDTO createOrder(CreateOrderDTO order, Long userId) {
    log.info("Creating order for user {}", userId);

    // Создаем Order
    Order orderForSave = new Order();
    orderForSave.setCreatedAt(LocalDateTime.now());
    orderForSave.setStatus(Status.CREATED);
    orderForSave.setTotalCost(order.getTotalCost());
    orderForSave.setUserId(userId);

    List<OrderItem> orderItems = new ArrayList<>();

    for (OrderItemDTO itemDTO : order.getOrderItems()) {
      Product product = productRepository.findById(itemDTO.getProductDTO().getId())
              .orElseThrow(() -> new ProductNotFoundException(itemDTO.getProductDTO().getId()));

      OrderItem orderItem = new OrderItem();
      orderItem.setProduct(product);
      orderItem.setQuantity(itemDTO.getQuantity());
      orderItem.setOrder(orderForSave);

      orderItems.add(orderItem);
    }

    orderForSave.setOrderItems(orderItems);

    Order savedOrder = orderRepository.save(orderForSave);

    CreateOrderEvent event = new CreateOrderEvent();
    event.setOrderId(savedOrder.getId());
    event.setUserId(userId);
    event.setAmount(order.getTotalCost());
    event.setPaymentType(PaymentType.BAR_BUY);

    kafkaTemplate.send("CREATE_ORDER_EVENT", event);

    return orderMapper.toDto(savedOrder);
  }

  @Override
  @Cacheable(value = "orders", key = "#id")
  public OrderDTO getOrderById(Long id) {
    if(!orderRepository.existsById(id)) {
      throw new OrderNotFoundException(id);
    }
    return orderMapper.toDto(orderRepository.getOrderById((id)));
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
    Order order = orderRepository.findById(orderId)
        .orElseThrow(()->new OrderNotFoundException(orderId));
    order.setStatus(newStatus);
    return orderMapper.toDto(orderRepository.save(order));
  }

  @Override
  public void updateStatusByEvent(CreatePaymentEvent event) {
    Order order = orderRepository.findById(event.getOrderId())
        .orElseThrow(()->new OrderNotFoundException(event.getOrderId()));
    order.setPaymentId(event.getPaymentId());
    order.setStatus(statusHandler(event.getStatus()));
    orderRepository.save(order);
  }

  private Status statusHandler(OperationType operationType) {
    return operationType.equals(OperationType.SUCCESS) ? Status.PAID : Status.ERROR_IN_PAID;
  }
}
