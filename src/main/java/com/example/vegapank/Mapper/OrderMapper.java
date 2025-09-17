package com.example.vegapank.Mapper;
import com.example.vegapank.DTO.OrderDTO;
import com.example.vegapank.Model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  OrderDTO toDTO(Order order);

  Order toEntity(OrderDTO orderDTO);
}
