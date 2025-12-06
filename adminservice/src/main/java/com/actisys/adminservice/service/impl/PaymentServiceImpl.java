package com.actisys.adminservice.service.impl;

import com.actisys.adminservice.client.BillingServiceClient;
import com.actisys.adminservice.client.InventoryServiceClient;
import com.actisys.adminservice.client.PaymentServiceClient;
import com.actisys.adminservice.client.ProductServiceClient;
import com.actisys.adminservice.client.UserServiceClient;
import com.actisys.adminservice.dto.orderDtos.OrderDTO;
import com.actisys.adminservice.dto.paymentDtos.AllPaymentDTO;
import com.actisys.adminservice.dto.sessionDtos.PCDTO;
import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import com.actisys.adminservice.service.PaymentService;
import com.actisys.common.events.PaymentType;
import com.actisys.common.user.UserDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentServiceClient paymentServiceClient;
  private final UserServiceClient userServiceClient;
  private final BillingServiceClient billingServiceClient;
  private final ProductServiceClient productServiceClient;
  private final InventoryServiceClient inventoryServiceClient;

  @Override
  public Mono<List<AllPaymentDTO>> getAllPayments() {
    return paymentServiceClient.getAllPayments()
        .flatMapMany(Flux::fromIterable)
        .flatMap(paymentDTO -> {
          Mono<UserDTO> userMono = userServiceClient.getUserById(paymentDTO.getUserId());
          Mono<OrderDTO> orderMono = Mono.empty();
          Mono<SessionDTO> sessionMono = Mono.empty();
          Mono<PCDTO> pcMono = Mono.empty();
          if(paymentDTO.getPaymentType() == PaymentType.BAR_BUY){
            orderMono = productServiceClient.getOrderById(paymentDTO.getOrderId())
                .switchIfEmpty(Mono.empty());
          }else if(paymentDTO.getPaymentType() == PaymentType.BOOKING){
            sessionMono = billingServiceClient.getSessionById(paymentDTO.getOrderId())
                .switchIfEmpty(Mono.empty());
            pcMono = sessionMono.filter(sessionDTO -> sessionDTO.getPcId() != null)
                .map(SessionDTO::getPcId)
                .flatMap(inventoryServiceClient::getPcById)
                .switchIfEmpty(Mono.empty());
          }
          return Mono.zip(
              userMono,
              orderMono.defaultIfEmpty(null),
              sessionMono.defaultIfEmpty(null),
              pcMono.defaultIfEmpty(null)
          ) .map(tuple-> AllPaymentDTO.builder()
              .paymentDTO(paymentDTO)
              .userDTO(tuple.getT1())
              .orderDTO(tuple.getT2())
              .sessionDTO(tuple.getT3())
              .pcDTO(tuple.getT4())
              .build());
        })
        .collectList();
  }
}
