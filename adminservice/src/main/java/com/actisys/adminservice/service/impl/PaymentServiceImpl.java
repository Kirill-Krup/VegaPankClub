package com.actisys.adminservice.service.impl;

import com.actisys.adminservice.client.BillingServiceClient;
import com.actisys.adminservice.client.InventoryServiceClient;
import com.actisys.adminservice.client.PaymentServiceClient;
import com.actisys.adminservice.client.ProductServiceClient;
import com.actisys.adminservice.client.UserServiceClient;
import com.actisys.adminservice.dto.paymentDtos.AllPaymentDTO;
import com.actisys.adminservice.service.PaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    return null;
  }
}
