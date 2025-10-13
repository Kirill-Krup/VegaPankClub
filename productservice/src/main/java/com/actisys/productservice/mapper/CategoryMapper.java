package com.actisys.productservice.mapper;

import com.actisys.productservice.dto.CategoryDTO;
import com.actisys.productservice.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  Category toEntity(CategoryDTO dto);

  CategoryDTO toDto(Category entity);
}
