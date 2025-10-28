package com.actisys.productservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private ProductServiceImpl productService;

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

    productDTO = new ProductDTO(1L, "Spring in Action", BigDecimal.valueOf(50.0),
        new com.actisys.productservice.dto.CategoryDTO(category.getCategoryId(), category.getName()), true);
  }

  @Test
  @DisplayName("getAllProducts should return list of ProductDTOs")
  void testGetAllProducts() {
    when(productRepository.findAll()).thenReturn(List.of(product));
    when(productMapper.toDTO(product)).thenReturn(productDTO);

    List<ProductDTO> result = productService.getAllProducts();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Spring in Action");
    verify(productRepository, times(1)).findAll();
    verify(productMapper, times(1)).toDTO(product);
  }

  @Test
  @DisplayName("addNewProduct should save product when category exists")
  void testAddNewProduct_Success() {
    ProductCreateDTO createDTO = new ProductCreateDTO("Spring in Action", BigDecimal.valueOf(50.0), 1L);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toDTO(product)).thenReturn(productDTO);

    ProductDTO result = productService.addNewProduct(createDTO);

    assertThat(result.getName()).isEqualTo("Spring in Action");
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("addNewProduct should throw exception when category does not exist")
  void testAddNewProduct_CategoryNotFound() {
    ProductCreateDTO createDTO = new ProductCreateDTO("Spring in Action", BigDecimal.valueOf(50.0), 1L);
    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.addNewProduct(createDTO))
        .isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining("1");

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  @DisplayName("updateProduct should update product when exists")
  void testUpdateProduct_Success() {
    ProductUpdateDTO updateDTO = new ProductUpdateDTO("Spring Boot", BigDecimal.valueOf(55.0), 1L, true);
    when(productRepository.existsById(1L)).thenReturn(false);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(product)).thenReturn(product);
    when(productMapper.toDTO(product)).thenReturn(productDTO);

    ProductDTO result = productService.updateProduct(1L, updateDTO);

    assertThat(result.getName()).isEqualTo("Spring in Action");
    verify(productRepository, times(1)).save(product);
  }

  @Test
  @DisplayName("updateProduct should throw exception when product does not exist")
  void testUpdateProduct_NotFound() {
    ProductUpdateDTO updateDTO = new ProductUpdateDTO("Spring Boot", BigDecimal.valueOf(55.0), 1L, true);
    when(productRepository.existsById(1L)).thenReturn(true);

    assertThatThrownBy(() -> productService.updateProduct(1L, updateDTO))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining("1");

    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("deleteProduct should delete product when exists")
  void testDeleteProduct_Success() {
    when(productRepository.existsById(1L)).thenReturn(true);

    productService.deleteProduct(1L);

    verify(productRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("deleteProduct should throw exception when product does not exist")
  void testDeleteProduct_NotFound() {
    when(productRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> productService.deleteProduct(1L))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining("1");

    verify(productRepository, never()).deleteById(anyLong());
  }

  @Test
  @DisplayName("getAllProducts should return empty list when no products")
  void testGetAllProducts_Empty() {
    when(productRepository.findAll()).thenReturn(List.of());

    List<ProductDTO> result = productService.getAllProducts();

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("addNewProduct sets category correctly")
  void testAddNewProduct_CategoryMapping() {
    ProductCreateDTO createDTO = new ProductCreateDTO("Spring in Action", BigDecimal.valueOf(50.0), 1L);
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toDTO(product)).thenReturn(productDTO);

    ProductDTO result = productService.addNewProduct(createDTO);

    assertThat(result.getCategory().getName()).isEqualTo("Books");
  }

  @Test
  @DisplayName("updateProduct sets category correctly")
  void testUpdateProduct_CategoryMapping() {
    ProductUpdateDTO updateDTO = new ProductUpdateDTO("Spring Boot", BigDecimal.valueOf(55.0), 1L, true);
    when(productRepository.existsById(1L)).thenReturn(false);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(product)).thenReturn(product);
    when(productMapper.toDTO(product)).thenReturn(productDTO);

    ProductDTO result = productService.updateProduct(1L, updateDTO);

    assertThat(result.getCategory().getName()).isEqualTo("Books");
  }

  @Test
  @DisplayName("updateProduct sets availability correctly")
  void testUpdateProduct_Availability() {
    ProductUpdateDTO updateDTO = new ProductUpdateDTO("Spring Boot", BigDecimal.valueOf(55.0), 1L, false);
    when(productRepository.existsById(1L)).thenReturn(false);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(productRepository.save(product)).thenReturn(product);
    when(productMapper.toDTO(product)).thenReturn(productDTO);

    ProductDTO result = productService.updateProduct(1L, updateDTO);

    assertThat(result.getIsAvailable()).isEqualTo(true);
  }
}
