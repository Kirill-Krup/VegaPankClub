package com.actisys.productservice.UnitTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.actisys.productservice.dto.CategoryDTO;
import com.actisys.productservice.exception.CategoryAlreadyException;
import com.actisys.productservice.exception.CategoryNotFoundException;
import com.actisys.productservice.mapper.CategoryMapper;
import com.actisys.productservice.model.Category;
import com.actisys.productservice.repository.CategoryRepository;
import com.actisys.productservice.service.impl.CategoryServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CategoryServiceUnitTests {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryServiceImpl categoryService;

  private Category category;
  private CategoryDTO categoryDTO;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    category = new Category();
    category.setCategoryId(1L);
    category.setName("Books");

    categoryDTO = new CategoryDTO(1L, "Books");
  }

  @Test
  @DisplayName("getAllCategies should return list of CategoryDTOs")
  void testGetAllCategories() {
    when(categoryRepository.findAll()).thenReturn(List.of(category));
    when(categoryMapper.toDto(category)).thenReturn(categoryDTO);

    List<CategoryDTO> result = categoryService.getAllCategies();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Books");
    verify(categoryRepository, times(1)).findAll();
    verify(categoryMapper, times(1)).toDto(category);
  }

  @Test
  @DisplayName("addNewCategory should save category when name is not taken")
  void testAddNewCategory_Success() {
    when(categoryRepository.existsByName("Books")).thenReturn(false);
    when(categoryRepository.save(any(Category.class))).thenReturn(category);
    when(categoryMapper.toDto(category)).thenReturn(categoryDTO);

    CategoryDTO result = categoryService.addNewCategory("Books");

    assertThat(result.getName()).isEqualTo("Books");
    verify(categoryRepository, times(1)).save(any(Category.class));
    verify(categoryMapper, times(1)).toDto(category);
  }

  @Test
  @DisplayName("addNewCategory should throw exception when category already exists")
  void testAddNewCategory_AlreadyExists() {
    when(categoryRepository.existsByName("Books")).thenReturn(true);

    assertThatThrownBy(() -> categoryService.addNewCategory("Books"))
        .isInstanceOf(CategoryAlreadyException.class)
        .hasMessageContaining("Books");

    verify(categoryRepository, never()).save(any(Category.class));
    verify(categoryMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("deleteCategory should delete category when exists")
  void testDeleteCategory_Success() {
    when(categoryRepository.existsById(1L)).thenReturn(true);

    categoryService.deleteCategory(1L);

    verify(categoryRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("deleteCategory should throw exception when category does not exist")
  void testDeleteCategory_NotFound() {
    when(categoryRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> categoryService.deleteCategory(1L))
        .isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining("1");

    verify(categoryRepository, never()).deleteById(1L);
  }
}
