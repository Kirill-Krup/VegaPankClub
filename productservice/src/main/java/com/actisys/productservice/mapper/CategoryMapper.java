package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.CategoryDTO;
import com.actisys.productservice.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  CategoryDTO toDto(Category category);

  Category toEntity(CategoryDTO dto);
}
