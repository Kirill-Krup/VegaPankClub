package com.example.vegapank.Mapper;
import com.example.vegapank.DTO.OrderItemDTO;
import com.example.vegapank.Model.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

  OrderItemDTO toDTO(OrderItem orderItem);

  OrderItem toEntity(OrderItemDTO orderItemDTO);
}
