package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.OrderDtos.OrderItemDTO;
import com.actisys.productservice.model.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

  OrderItem toEntity(OrderItemDTO orderItemDTO);

  OrderItemDTO toDTO(OrderItem orderItem);

  List<OrderItem> toEntityList(List<OrderItemDTO> orderItems);
}
