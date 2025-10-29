package com.actisys.productservice.service.impl;

import com.actisys.productservice.dto.CategoryDTO;
import com.actisys.productservice.exception.CategoryAlreadyException;
import com.actisys.productservice.exception.CategoryNotFoundException;
import com.actisys.productservice.mapper.CategoryMapper;
import com.actisys.productservice.model.Category;
import com.actisys.productservice.repository.CategoryRepository;
import com.actisys.productservice.service.CategoryService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

  private final CategoryMapper categoryMapper;
  private final CategoryRepository categoryRepository;

  public CategoryServiceImpl(CategoryMapper categoryMapper, CategoryRepository categoryRepository) {
    this.categoryMapper = categoryMapper;
    this.categoryRepository = categoryRepository;
  }

  @Override
  public List<CategoryDTO> getAllCategies() {
    List<Category> categories = categoryRepository.findAll();
    return categories.stream().map(categoryMapper::toDto).collect(Collectors.toList());
  }

  @Override
  public CategoryDTO addNewCategory(String name) {
    if(categoryRepository.existsByName(name)) {
      throw new CategoryAlreadyException(name);
    }
    Category category = new Category();
    category.setName(name);
    return categoryMapper.toDto(categoryRepository.save(category));
  }

  @Override
  public void deleteCategory(Long id) {
    if (!categoryRepository.existsById(id)) {
      throw new CategoryNotFoundException(id);
    }
    categoryRepository.deleteById(id);
  }
}
