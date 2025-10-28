package com.actisys.productservice.controller;

import com.actisys.productservice.dto.CategoryDTO;
import com.actisys.productservice.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {
  private final CategoryService categoryService;

  @GetMapping("/getAllCategories")
  public ResponseEntity<List<CategoryDTO>> getAllCategories() {
    List<CategoryDTO> allCategories = categoryService.getAllCategies();
    return ResponseEntity.ok(allCategories);
  }

  @PostMapping("/addNewCategory/{name}")
  public ResponseEntity<CategoryDTO> addNewCategory(@PathVariable String name) {
    CategoryDTO addedCategory = categoryService.addNewCategory(name);
    return ResponseEntity.ok(addedCategory);
  }

  @DeleteMapping("/deleteCategory/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}
