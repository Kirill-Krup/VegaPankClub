package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ProductMapper {

  Product toEntity(ProductDTO productDTO);

  ProductDTO toDTO(Product product);
}
