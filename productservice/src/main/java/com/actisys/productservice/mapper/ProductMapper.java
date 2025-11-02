package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ProductMapper {

  @Mapping(source = "category", target = "category")
  ProductDTO toDTO(Product product);

  Product toEntity(ProductDTO productDTO);
}
