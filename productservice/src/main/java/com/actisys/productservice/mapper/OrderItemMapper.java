package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.OrderDtos.OrderItemDTO;
import com.actisys.productservice.model.OrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderItemMapper {

  OrderItem toEntity(OrderItemDTO orderItemDTO);

  @Mapping(target = "productDTO", source = "product")
  OrderItemDTO toDTO(OrderItem orderItem);

  List<OrderItem> toEntityList(List<OrderItemDTO> orderItems);
}
