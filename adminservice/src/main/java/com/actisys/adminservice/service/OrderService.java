package com.actisys.adminservice.service;

import com.actisys.adminservice.dto.orderDtos.AllOrderDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface OrderService {

  Mono<List<AllOrderDTO>> getAllOrders();
}
