package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.OrderDtos.OrderDTO;
import com.actisys.productservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

  @Mapping(target = "userId", ignore = true)
  Order toEntity(OrderDTO dto);

  @Mapping(target = "userDTO", ignore = true)
  OrderDTO toDto(Order entity);
}
