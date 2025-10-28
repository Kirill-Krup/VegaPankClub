package com.actisys.productservice.service;

import com.actisys.productservice.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {

  List<CategoryDTO> getAllCategies();

  CategoryDTO addNewCategory(String name);

  void deleteCategory(Long id);
}
