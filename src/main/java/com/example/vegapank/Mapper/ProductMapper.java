package com.example.vegapank.Mapper;
import com.example.vegapank.DTO.ProductDTO;
import com.example.vegapank.Model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  ProductDTO toDTO(Product product);

  Product toEntity(ProductDTO productDTO);
}
