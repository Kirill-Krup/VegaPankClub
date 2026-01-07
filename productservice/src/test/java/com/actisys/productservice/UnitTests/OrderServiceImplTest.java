package com.actisys.productservice.UnitTests;

import com.actisys.common.events.OperationType;
import com.actisys.common.events.PaymentType;
import com.actisys.common.events.order.CreateOrderEvent;
import com.actisys.common.events.payment.CreatePaymentEvent;
import com.actisys.productservice.dto.OrderDtos.CreateOrderDTO;
import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.dto.OrderDtos.OrderItemDTO;
import com.actisys.productservice.dto.ProductDtos.ProductDTO;
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
import com.actisys.productservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

  @Mock
  private OrderMapper orderMapper;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks
  private OrderServiceImpl orderService;

  private Order testOrder;
  private OrderDTO testOrderDTO;
  private CreateOrderDTO createOrderDTO;
  private List<OrderItemDTO> orderItemDTOs;

  @BeforeEach
  void setUp() {
    Product testProduct = new Product();
    testProduct.setId(101L);
    testProduct.setName("Test Product");
    testProduct.setPrice(new BigDecimal("50.00"));

    ProductDTO testProductDTO = new ProductDTO();
    testProductDTO.setId(101L);
    testProductDTO.setName("Test Product");
    testProductDTO.setPrice(new BigDecimal("50.00"));

    OrderItemDTO orderItemDTO1 = new OrderItemDTO(1L, null, testProductDTO, new BigDecimal("50.00"), 2);
    OrderItemDTO orderItemDTO2 = new OrderItemDTO(2L, null, testProductDTO, new BigDecimal("50.00"), 1);

    orderItemDTOs = List.of(orderItemDTO1, orderItemDTO2);

    OrderItem orderItem1 = new OrderItem();
    orderItem1.setOrderItemId(1L);
    orderItem1.setProduct(testProduct);
    orderItem1.setQuantity(2);

    OrderItem orderItem2 = new OrderItem();
    orderItem2.setOrderItemId(2L);
    orderItem2.setProduct(testProduct);
    orderItem2.setQuantity(1);

    List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

    testOrder = new Order();
    testOrder.setId(1L);
    testOrder.setUserId(123L);
    testOrder.setTotalCost(new BigDecimal("150.00"));
    testOrder.setStatus(Status.CREATED);
    testOrder.setCreatedAt(LocalDateTime.now());
    testOrder.setOrderItems(orderItems);
    testOrder.setPaymentId(null);

    testOrderDTO = new OrderDTO();
    testOrderDTO.setId(1L);
    testOrderDTO.setUserId(123L);
    testOrderDTO.setTotalCost(new BigDecimal("150.00"));
    testOrderDTO.setStatus(Status.CREATED);
    testOrderDTO.setCreatedAt(LocalDateTime.now());
    testOrderDTO.setPaymentId(null);

    createOrderDTO = new CreateOrderDTO();
    createOrderDTO.setOrderItems(orderItemDTOs);
    createOrderDTO.setTotalCost(new BigDecimal("150.00"));
  }

  @Test
  @DisplayName("Should create order successfully and send Kafka event")
  void testCreateOrderSuccess() {
    Long userId = 123L;

    Product testProduct = new Product();
    testProduct.setId(101L);
    testProduct.setName("Test Product");
    testProduct.setPrice(new BigDecimal("50.00"));

    when(productRepository.findById(101L)).thenReturn(Optional.of(testProduct));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
    when(orderMapper.toDto(testOrder)).thenReturn(testOrderDTO);

    OrderDTO result = orderService.createOrder(createOrderDTO, userId);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(userId, result.getUserId());
    assertEquals(Status.CREATED, result.getStatus());

    verify(productRepository, times(2)).findById(101L);
    verify(orderRepository, times(1)).save(any(Order.class));

    ArgumentCaptor<CreateOrderEvent> eventCaptor = ArgumentCaptor.forClass(CreateOrderEvent.class);
    verify(kafkaTemplate, times(1)).send(eq("CREATE_ORDER_EVENT"), eventCaptor.capture());

    CreateOrderEvent sentEvent = eventCaptor.getValue();
    assertEquals(testOrder.getId(), sentEvent.getOrderId());
    assertEquals(userId, sentEvent.getUserId());
    assertEquals(createOrderDTO.getTotalCost(), sentEvent.getAmount());
    assertEquals(PaymentType.BAR_BUY, sentEvent.getPaymentType());
  }

  @Test
  @DisplayName("Should throw ProductNotFoundException when product not found")
  void testCreateOrderProductNotFound() {
    Long userId = 123L;

    when(productRepository.findById(101L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(createOrderDTO, userId));

    verify(productRepository, times(1)).findById(101L);
    verify(orderRepository, never()).save(any());
    verify(kafkaTemplate, never()).send(any(), any());
  }

  @Test
  @DisplayName("Should get order by id successfully")
  void testGetOrderByIdSuccess() {
    Long orderId = 1L;
    when(orderRepository.existsById(orderId)).thenReturn(true);
    when(orderRepository.getOrderById(orderId)).thenReturn(testOrder);
    when(orderMapper.toDto(testOrder)).thenReturn(testOrderDTO);

    OrderDTO result = orderService.getOrderById(orderId);

    assertNotNull(result);
    assertEquals(orderId, result.getId());
    verify(orderRepository, times(1)).existsById(orderId);
    verify(orderRepository, times(1)).getOrderById(orderId);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order not found by id")
  void testGetOrderByIdNotFound() {
    Long orderId = 999L;
    when(orderRepository.existsById(orderId)).thenReturn(false);

    assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
    verify(orderRepository, times(1)).existsById(orderId);
    verify(orderRepository, never()).getOrderById(any());
  }

  @Test
  @DisplayName("Should get all orders successfully")
  void testGetAllOrdersSuccess() {
    List<Order> orders = List.of(testOrder);
    when(orderRepository.findAll()).thenReturn(orders);
    when(orderMapper.toDto(testOrder)).thenReturn(testOrderDTO);

    List<OrderDTO> result = orderService.getAllOrders();

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(orderRepository, times(1)).findAll();
    verify(orderMapper, times(1)).toDto(testOrder);
  }

  @Test
  @DisplayName("Should return empty list when no orders exist")
  void testGetAllOrdersEmpty() {
    when(orderRepository.findAll()).thenReturn(List.of());

    List<OrderDTO> result = orderService.getAllOrders();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(orderRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should get orders by status successfully")
  void testGetOrdersByStatusSuccess() {
    Status status = Status.CREATED;
    List<Order> orders = List.of(testOrder);
    when(orderRepository.getOrdersByStatus(status)).thenReturn(orders);
    when(orderMapper.toDto(testOrder)).thenReturn(testOrderDTO);

    List<OrderDTO> result = orderService.getOrdersByStatus(status);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(status, result.get(0).getStatus());
    verify(orderRepository, times(1)).getOrdersByStatus(status);
  }

  @Test
  @DisplayName("Should return empty list when no orders with specified status")
  void testGetOrdersByStatusEmpty() {
    Status status = Status.PAID;
    when(orderRepository.getOrdersByStatus(status)).thenReturn(List.of());

    List<OrderDTO> result = orderService.getOrdersByStatus(status);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(orderRepository, times(1)).getOrdersByStatus(status);
  }

  @Test
  @DisplayName("Should delete order successfully")
  void testDeleteOrderSuccess() {
    Long orderId = 1L;
    when(orderRepository.existsById(orderId)).thenReturn(true);

    orderService.deleteOrder(orderId);

    verify(orderRepository, times(1)).existsById(orderId);
    verify(orderRepository, times(1)).deleteById(orderId);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when deleting non-existent order")
  void testDeleteOrderNotFound() {
    Long orderId = 999L;
    when(orderRepository.existsById(orderId)).thenReturn(false);

    assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(orderId));
    verify(orderRepository, times(1)).existsById(orderId);
    verify(orderRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should update order status successfully")
  void testUpdateOrderStatusSuccess() {
    Long orderId = 1L;
    Status newStatus = Status.PAID;

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(testOrder)).thenReturn(testOrder);
    when(orderMapper.toDto(testOrder)).thenReturn(testOrderDTO);

    OrderDTO result = orderService.updateOrderStatus(orderId, newStatus);

    assertNotNull(result);
    assertEquals(newStatus, testOrder.getStatus());
    verify(orderRepository, times(1)).findById(orderId);
    verify(orderRepository, times(1)).save(testOrder);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when updating status of non-existent order")
  void testUpdateOrderStatusNotFound() {
    Long orderId = 999L;
    Status newStatus = Status.PAID;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class,
            () -> orderService.updateOrderStatus(orderId, newStatus));
    verify(orderRepository, times(1)).findById(orderId);
    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update order status by event with SUCCESS operation")
  void testUpdateStatusByEventSuccess() {
    CreatePaymentEvent event = new CreatePaymentEvent();
    event.setPaymentId(456L);
    event.setOrderId(1L);
    event.setStatus(OperationType.SUCCESS);

    when(orderRepository.findById(event.getOrderId())).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(testOrder)).thenReturn(testOrder);

    orderService.updateStatusByEvent(event);

    assertEquals(Status.PAID, testOrder.getStatus());
    assertEquals(456L, testOrder.getPaymentId());
    verify(orderRepository, times(1)).findById(event.getOrderId());
    verify(orderRepository, times(1)).save(testOrder);
  }

  @Test
  @DisplayName("Should update order status by event with ERROR operation")
  void testUpdateStatusByEventError() {
    CreatePaymentEvent event = new CreatePaymentEvent();
    event.setPaymentId(456L);
    event.setOrderId(1L);
    event.setStatus(OperationType.ERROR);

    when(orderRepository.findById(event.getOrderId())).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(testOrder)).thenReturn(testOrder);

    orderService.updateStatusByEvent(event);

    assertEquals(Status.ERROR_IN_PAID, testOrder.getStatus());
    assertEquals(456L, testOrder.getPaymentId());
    verify(orderRepository, times(1)).findById(event.getOrderId());
    verify(orderRepository, times(1)).save(testOrder);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when updating status by event for non-existent order")
  void testUpdateStatusByEventNotFound() {
    CreatePaymentEvent event = new CreatePaymentEvent();
    event.setOrderId(999L);
    when(orderRepository.findById(event.getOrderId())).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService.updateStatusByEvent(event));
    verify(orderRepository, times(1)).findById(event.getOrderId());
    verify(orderRepository, never()).save(any());
  }

}