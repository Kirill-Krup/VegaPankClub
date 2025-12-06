package com.actisys.adminservice.client;

import com.actisys.adminservice.dto.orderDtos.OrderDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ProductServiceClient {

  Mono<List<OrderDTO>> getAllOrders();

  Mono<OrderDTO> getOrderById(Long orderId);
}
