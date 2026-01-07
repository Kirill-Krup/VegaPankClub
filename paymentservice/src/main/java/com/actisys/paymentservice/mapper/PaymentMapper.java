package com.actisys.paymentservice.mapper;

import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.dto.PaymentIDDTO;
import com.actisys.paymentservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
  Payment toEntity(PaymentDTO paymentDTO);

  PaymentDTO toDto(Payment payment);
  PaymentIDDTO toIDDto(Payment payment);
}
