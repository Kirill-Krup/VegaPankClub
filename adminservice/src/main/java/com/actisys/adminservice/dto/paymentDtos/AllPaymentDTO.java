package com.actisys.adminservice.dto.paymentDtos;

import com.actisys.adminservice.dto.orderDtos.OrderDTO;
import com.actisys.adminservice.dto.sessionDtos.PCDTO;
import com.actisys.adminservice.dto.sessionDtos.SessionDTO;
import com.actisys.common.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllPaymentDTO {
  private PaymentIDDTO paymentDTO;
  private UserDTO userDTO;
  private OrderDTO orderDTO;
  private SessionDTO sessionDTO;
  private PCDTO pcDTO;
}
