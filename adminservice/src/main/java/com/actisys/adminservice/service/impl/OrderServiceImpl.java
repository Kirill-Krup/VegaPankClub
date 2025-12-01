package com.actisys.adminservice.service.impl;

import com.actisys.adminservice.client.PaymentServiceClient;
import com.actisys.adminservice.client.ProductServiceClient;
import com.actisys.adminservice.client.UserServiceClient;
import com.actisys.adminservice.dto.orderDtos.AllOrderDTO;
import com.actisys.adminservice.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final ProductServiceClient productServiceClient;
  private final UserServiceClient userServiceClient;
  private final PaymentServiceClient paymentServiceClient;

  @Override
  public Mono<List<AllOrderDTO>> getAllOrders() {
    return productServiceClient.getAllOrders()
        .flatMapMany(Flux::fromIterable)
        .flatMap(order->
            Mono.zip(
                Mono.just(order),
                paymentServiceClient.getPaymentById(order.getPaymentId()),
                userServiceClient.getUserById(order.getUserId())
            ).map(tuple -> AllOrderDTO.builder()
                .order(tuple.getT1())
                .payment(tuple.getT2())
                .user(tuple.getT3())
                .build()
            )
        )
        .collectList();
  }
}
