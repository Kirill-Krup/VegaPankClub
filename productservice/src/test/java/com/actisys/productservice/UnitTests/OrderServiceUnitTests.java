package com.actisys.productservice.UnitTests;

import com.actisys.common.dto.user.UserDTO;
import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.dto.OrderDtos.OrderItemDTO;
import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.dto.Status;
import com.actisys.productservice.exception.OrderNotFoundException;
import com.actisys.productservice.mapper.OrderMapper;
import com.actisys.productservice.model.Order;
import com.actisys.productservice.repository.OrderRepository;
import com.actisys.productservice.service.impl.OrderServiceImpl;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceUnitTests {

  @Mock
  private OrderMapper orderMapper;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderServiceImpl orderService;

  private Order order;
  private OrderDTO orderDTO;
  private UserDTO userDTO;
  private ProductDTO productDTO;
  private OrderItemDTO orderItemDTO;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    userDTO = new UserDTO(
        1L,
        "testuser",
        "test@example.com",
        "+375445797515",
        "Krupenin Kirill",
        0.0,
        "",
        0,
        Timestamp.valueOf(LocalDateTime.now()),
        Timestamp.valueOf("2005-12-19 00:00:00"),
        Timestamp.valueOf("2025-10-28 00:00:00"),
        true,
        false
    );    productDTO = new ProductDTO(1L, "Book", BigDecimal.valueOf(15.5), null, true);
    orderItemDTO = new OrderItemDTO(1L, 1L, productDTO, BigDecimal.valueOf(15.5), 2);

    order = new Order();
    order.setOrderId(1L);
    order.setUserId(1L);
    order.setCreatedAt(LocalDateTime.now());
    order.setStatus(Status.PAID);
    order.setTotalCost(BigDecimal.valueOf(31));

    orderDTO = new OrderDTO(
        1L,
        userDTO,
        order.getCreatedAt(),
        Status.PAID,
        BigDecimal.valueOf(31),
        List.of(orderItemDTO)
    );
  }

  @Test
  @DisplayName("createOrder() should create order and return dto")
  void testCreateOrder_Success() {
    when(orderMapper.toEntity(orderDTO)).thenReturn(order);
    when(orderRepository.save(order)).thenReturn(order);
    when(orderMapper.toDto(order)).thenReturn(orderDTO);

    OrderDTO result = orderService.createOrder(orderDTO);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(Status.PAID);
    verify(orderRepository).save(order);
    verify(orderMapper).toEntity(orderDTO);
    verify(orderMapper).toDto(order);
  }

  @Test
  @DisplayName("getOrderById() should return dto")
  void testGetOrderById_Success() {
    when(orderRepository.existsById(1L)).thenReturn(true);
    when(orderRepository.getOrderByOrderId(1L)).thenReturn(order);
    when(orderMapper.toDto(order)).thenReturn(orderDTO);

    OrderDTO result = orderService.getOrderById(1L);

    assertThat(result.getOrderId()).isEqualTo(1L);
    assertThat(result.getUserDTO().getLogin()).isEqualTo("testuser");
    verify(orderRepository).existsById(1L);
    verify(orderRepository).getOrderByOrderId(1L);
    verify(orderMapper).toDto(order);
  }

  @Test
  @DisplayName("getOrderById() should throw exception if order is not exists")
  void testGetOrderById_NotFound() {
    when(orderRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> orderService.getOrderById(1L))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("1");

    verify(orderRepository).existsById(1L);
    verify(orderRepository, never()).getOrderByOrderId(anyLong());
  }

  @Test
  @DisplayName("getAllOrders() should return dto list")
  void testGetAllOrders_Success() {
    when(orderRepository.findAll()).thenReturn(List.of(order));
    when(orderMapper.toDto(order)).thenReturn(orderDTO);

    List<OrderDTO> result = orderService.getAllOrders();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTotalCost()).isEqualTo(BigDecimal.valueOf(31));
    verify(orderRepository).findAll();
    verify(orderMapper).toDto(order);
  }

  @Test
  @DisplayName("getAllOrders() should return empty list")
  void testGetAllOrders_Empty() {
    when(orderRepository.findAll()).thenReturn(List.of());

    List<OrderDTO> result = orderService.getAllOrders();

    assertThat(result).isEmpty();
    verify(orderRepository).findAll();
    verify(orderMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("getOrdersByStatus() should return orders by statuses")
  void testGetOrdersByStatus_Success() {
    when(orderRepository.getOrdersByStatus(Status.PAID)).thenReturn(List.of(order));
    when(orderMapper.toDto(order)).thenReturn(orderDTO);

    List<OrderDTO> result = orderService.getOrdersByStatus(Status.PAID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStatus()).isEqualTo(Status.PAID);
    verify(orderRepository).getOrdersByStatus(Status.PAID);
  }

  @Test
  @DisplayName("getOrdersByStatus()should return empty list")
  void testGetOrdersByStatus_Empty() {
    when(orderRepository.getOrdersByStatus(Status.CANCELLED)).thenReturn(List.of());

    List<OrderDTO> result = orderService.getOrdersByStatus(Status.CANCELLED);

    assertThat(result).isEmpty();
    verify(orderRepository).getOrdersByStatus(Status.CANCELLED);
  }

  @Test
  @DisplayName("deleteOrder() should delete order")
  void testDeleteOrder_Success() {
    when(orderRepository.existsById(1L)).thenReturn(true);

    orderService.deleteOrder(1L);

    verify(orderRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteOrder() should throw exception if order is not exists")
  void testDeleteOrder_NotFound() {
    when(orderRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> orderService.deleteOrder(1L))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("1");

    verify(orderRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("updateOrderStatus() should update status of order")
  void testUpdateOrderStatus_Success() {
    when(orderRepository.existsById(1L)).thenReturn(true);
    when(orderRepository.getOrderByOrderId(1L)).thenReturn(order);
    when(orderRepository.save(order)).thenReturn(order);
    when(orderMapper.toDto(order)).thenReturn(orderDTO);

    OrderDTO result = orderService.updateOrderStatus(1L, Status.SHIPPED);

    assertThat(result).isNotNull();
    verify(orderRepository).save(order);
    assertThat(order.getStatus()).isEqualTo(Status.SHIPPED);
  }

  @Test
  @DisplayName("updateOrderStatus() should throw exception if order is not exists")
  void testUpdateOrderStatus_NotFound() {
    when(orderRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> orderService.updateOrderStatus(1L, Status.SHIPPED))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining("1");

    verify(orderRepository, never()).save(any());
  }
}
