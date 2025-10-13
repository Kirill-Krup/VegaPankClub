package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.OrderItemDTO;
import com.actisys.productservice.model.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

  OrderItem toEntity(OrderItem orderItem);

  OrderItemDTO toDTO(OrderItem orderItem);
}
