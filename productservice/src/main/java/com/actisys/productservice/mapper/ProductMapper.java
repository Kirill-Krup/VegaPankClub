package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.ProductDTO;
import com.actisys.productservice.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  Product toEntity(ProductDTO productDTO);

  ProductDTO toDTO(Product product);
}
