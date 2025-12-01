package com.actisys.adminservice.dto.orderDtos;

import com.actisys.adminservice.dto.PaymentDTO;
import com.actisys.common.user.UserDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AllOrderDTO {
  private OrderDTO order;
  private PaymentDTO payment;
  private UserDTO user;
}
