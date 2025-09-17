package com.example.vegapank.Mapper;
import com.example.vegapank.DTO.CategoryDTO;
import com.example.vegapank.Model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  CategoryDTO toDTO(Category category);

  Category toEntity(CategoryDTO categoryDTO);
}
