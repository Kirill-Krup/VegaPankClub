package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.OrderDTO;
import com.actisys.productservice.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

  Order toEntity(OrderDTO dto);

  OrderDTO toDto(Order entity);
}
