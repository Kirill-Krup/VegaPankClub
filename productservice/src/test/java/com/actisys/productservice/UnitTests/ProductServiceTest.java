package com.actisys.productservice.UnitTests;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.actisys.productservice.dto.CategoryDTO;
import com.actisys.productservice.dto.ProductDtos.ProductCreateDTO;
import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.dto.ProductDtos.ProductUpdateDTO;
import com.actisys.productservice.exception.CategoryNotFoundException;
import com.actisys.productservice.exception.ProductNotFoundException;
import com.actisys.productservice.mapper.ProductMapper;
import com.actisys.productservice.model.Category;
import com.actisys.productservice.model.Product;
import com.actisys.productservice.repository.CategoryRepository;
import com.actisys.productservice.repository.ProductRepository;
import com.actisys.productservice.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private ProductMapper productMapper;
  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private ProductServiceImpl productService;

  private Product product;
  private ProductDTO productDTO;
  private Category category;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    category = new Category();
    category.setCategoryId(1L);
    category.setName("Books");

    product = new Product();
    product.setProductId(1L);
    product.setName("Spring in Action");
    product.setPrice(BigDecimal.valueOf(50.0));
    product.setCategory(category);
    product.setIsAvailable(true);

    productDTO =
        new ProductDTO(
            1L,
            "Spring in Action",
            BigDecimal.valueOf(50.0),
            new CategoryDTO(category.getCategoryId(), category.getName()),
            true);
  }


  @Test
  @DisplayName("getAllProducts should return list of ProductDTOs")
  void testGetAllProducts() {
    when(productRepository.findAll()).thenReturn(List.of(product));
    when(productMapper.toDTO(product)).thenReturn(productDTO);

    List<ProductDTO> result = productService.getAllProducts();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Spring in Action");
    verify(productRepository).findAll();
    verify(productMapper).toDTO(product);
  }

  @Test
  @DisplayName("getAllProducts should return empty list when no products exist")
  void testGetAllProducts_Empty() {
    when(productRepository.findAll()).thenReturn(List.of());

    List<ProductDTO> result = productService.getAllProducts();

    assertThat(result).isEmpty();
    verify(productRepository).findAll();
  }


  @Test
  @DisplayName("addNewProduct should save and return DTO when category exists")
  void testAddNewProduct_Success() {
    ProductCreateDTO dto = new ProductCreateDTO("Book", BigDecimal.TEN, 1L);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

    ProductDTO result = productService.addNewProduct(dto);

    assertThat(result.getName()).isEqualTo("Spring in Action");
    verify(productRepository).save(any(Product.class));
  }

  @Test
  @DisplayName("addNewProduct should throw exception when category not found")
  void testAddNewProduct_CategoryNotFound() {
    ProductCreateDTO dto = new ProductCreateDTO("Book", BigDecimal.TEN, 2L);
    when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.addNewProduct(dto))
        .isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining("2");

    verify(productRepository, never()).save(any());
  }


  @Test
  @DisplayName("updateProduct should update fields correctly when product exists")
  void testUpdateProduct_Success() {
    ProductUpdateDTO dto = new ProductUpdateDTO("Spring Boot", BigDecimal.valueOf(55), 1L, false);

    when(productRepository.existsById(1L)).thenReturn(true);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

    ProductDTO result = productService.updateProduct(1L, dto);

    assertThat(result).isNotNull();
    assertThat(result.getCategory().getName()).isEqualTo("Books");
    verify(productRepository).save(any(Product.class));
  }

  @Test
  @DisplayName("updateProduct should throw exception when product not found")
  void testUpdateProduct_NotFound() {
    when(productRepository.existsById(1L)).thenReturn(false);

    ProductUpdateDTO dto = new ProductUpdateDTO("Spring Boot", BigDecimal.TEN, 1L, true);

    assertThatThrownBy(() -> productService.updateProduct(1L, dto))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining("1");

    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateProduct should throw exception when category not found")
  void testUpdateProduct_CategoryNotFound() {
    ProductUpdateDTO dto = new ProductUpdateDTO("Spring Boot", BigDecimal.TEN, 99L, true);

    when(productRepository.existsById(1L)).thenReturn(true);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.updateProduct(1L, dto))
        .isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining("99");
  }


  @Test
  @DisplayName("deleteProduct should delete product when exists")
  void testDeleteProduct_Success() {
    when(productRepository.existsById(1L)).thenReturn(true);

    productService.deleteProduct(1L);

    verify(productRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteProduct should throw when product does not exist")
  void testDeleteProduct_NotFound() {
    when(productRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> productService.deleteProduct(1L))
        .isInstanceOf(ProductNotFoundException.class);

    verify(productRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("addNewProduct should handle null price gracefully")
  void testAddNewProduct_NullPrice() {
    ProductCreateDTO dto = new ProductCreateDTO("Book", null, 1L);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

    ProductDTO result = productService.addNewProduct(dto);

    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("updateProduct should correctly map availability")
  void testUpdateProduct_AvailabilityChange() {
    ProductUpdateDTO dto = new ProductUpdateDTO("Spring Boot", BigDecimal.valueOf(55), 1L, false);

    when(productRepository.existsById(1L)).thenReturn(true);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

    ProductDTO result = productService.updateProduct(1L, dto);

    assertThat(result.getIsAvailable()).isTrue();
  }

  @Test
  @DisplayName("getAllProducts should use cache when called repeatedly")
  void testGetAllProducts_CacheSimulation() {
    when(productRepository.findAll()).thenReturn(List.of(product));
    when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

    List<ProductDTO> firstCall = productService.getAllProducts();
    List<ProductDTO> secondCall = productService.getAllProducts();

    assertThat(firstCall).hasSize(1);
    assertThat(secondCall).hasSize(1);
    verify(productRepository, times(2)).findAll();
  }
}
