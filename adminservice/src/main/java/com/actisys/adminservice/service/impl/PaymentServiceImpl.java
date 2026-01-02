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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
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
        .flatMap(paymentDTO ->
            userServiceClient.getUserById(paymentDTO.getUserId())
                .onErrorResume(e -> Mono.just(createEmptyUserDTO(paymentDTO.getUserId())))
                .flatMap(user -> {
                  if (paymentDTO.getOrderId() == null || paymentDTO.getOrderId() <= 0) {
                    return Mono.just(AllPaymentDTO.builder()
                        .paymentDTO(paymentDTO)
                        .userDTO(user)
                        .build());
                  }

                  if (paymentDTO.getPaymentType() == PaymentType.BAR_BUY) {
                    return productServiceClient.getOrderById(paymentDTO.getOrderId())
                        .onErrorResume(e -> Mono.empty())
                        .map(order -> AllPaymentDTO.builder()
                            .paymentDTO(paymentDTO)
                            .userDTO(user)
                            .orderDTO(order)
                            .build())
                        .defaultIfEmpty(AllPaymentDTO.builder()
                            .paymentDTO(paymentDTO)
                            .userDTO(user)
                            .build());
                  }

                  if (paymentDTO.getPaymentType() == PaymentType.BOOKING) {
                    return billingServiceClient.getSessionById(paymentDTO.getOrderId())
                        .onErrorResume(e -> Mono.empty())
                        .flatMap(session -> {
                          if (session.getPcId() == null) {
                            return Mono.just(AllPaymentDTO.builder()
                                .paymentDTO(paymentDTO)
                                .userDTO(user)
                                .sessionDTO(session)
                                .build());
                          }

                          return inventoryServiceClient.getPcById(session.getPcId())
                              .onErrorResume(e -> Mono.empty())
                              .map(pc -> AllPaymentDTO.builder()
                                  .paymentDTO(paymentDTO)
                                  .userDTO(user)
                                  .sessionDTO(session)
                                  .pcDTO(pc)
                                  .build())
                              .defaultIfEmpty(AllPaymentDTO.builder()
                                  .paymentDTO(paymentDTO)
                                  .userDTO(user)
                                  .sessionDTO(session)
                                  .build());
                        })
                        .defaultIfEmpty(AllPaymentDTO.builder()
                            .paymentDTO(paymentDTO)
                            .userDTO(user)
                            .build());
                  }

                  return Mono.just(AllPaymentDTO.builder()
                      .paymentDTO(paymentDTO)
                      .userDTO(user)
                      .build());
                })
        )
        .collectList();
  }

  private UserDTO createEmptyUserDTO(Long userId) {
    UserDTO dto = new UserDTO();
    dto.setId(userId);
    dto.setLogin("Не найден");
    return dto;
  }
}
