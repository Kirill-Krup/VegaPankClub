package com.actisys.productservice.UnitTests;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  private ProductServiceImpl productService;

  private Product testProduct;
  private ProductDTO testProductDTO;
  private Category testCategory;
  private CategoryDTO testCategoryDTO;
  private ProductCreateDTO productCreateDTO;
  private ProductUpdateDTO productUpdateDTO;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(productService, "uploadDir", "static/images");

    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Electronics");

    testCategoryDTO = testCategoryDTO.builder()
        .id(1L)
        .name("Electronics")
        .build();

    testProduct = new Product();
    testProduct.setId(1L);
    testProduct.setName("Test Product");
    testProduct.setPrice(new BigDecimal("99.99"));
    testProduct.setStock(100);
    testProduct.setPhotoPath("/static/images/test-image.jpg");
    testProduct.setActive(true);
    testProduct.setCategory(testCategory);

    testProductDTO = new ProductDTO();
    testProductDTO.setId(1L);
    testProductDTO.setName("Test Product");
    testProductDTO.setPrice(new BigDecimal("99.99"));
    testProductDTO.setStock(100);
    testProductDTO.setPhotoPath("/static/images/test-image.jpg");
    testProductDTO.setActive(true);
    testProductDTO.setCategory(testCategoryDTO);

    productCreateDTO = ProductCreateDTO.builder()
        .name("New Product")
        .price(new BigDecimal("49.99"))
        .stock(50)
        .active(true)
        .categoryId(1L)
        .build();

    productUpdateDTO = new ProductUpdateDTO(
        "Updated Product",
        new BigDecimal("79.99"),
        1L,
        true
    );
  }

  // ==================== getAllProducts ====================

  @Test
  @DisplayName("Should return all products successfully")
  void testGetAllProductsSuccess() {
    List<Product> productList = List.of(testProduct);

    when(productRepository.findAllWithCategory()).thenReturn(productList);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    List<ProductDTO> result = productService.getAllProducts();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Product", result.get(0).getName());
    verify(productRepository, times(1)).findAllWithCategory();
    verify(productMapper, times(1)).toDTO(testProduct);
  }

  @Test
  @DisplayName("Should return empty list when no products exist")
  void testGetAllProductsEmpty() {
    when(productRepository.findAllWithCategory()).thenReturn(List.of());

    List<ProductDTO> result = productService.getAllProducts();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(productRepository, times(1)).findAllWithCategory();
  }

  @Test
  @DisplayName("Should map multiple products correctly")
  void testGetAllProductsMultiple() {
    Product product2 = new Product();
    product2.setId(2L);
    product2.setName("Product 2");
    product2.setPrice(new BigDecimal("199.99"));
    product2.setCategory(testCategory);

    ProductDTO productDTO2 = new ProductDTO();
    productDTO2.setId(2L);
    productDTO2.setName("Product 2");
    productDTO2.setPrice(new BigDecimal("199.99"));
    productDTO2.setCategory(testCategoryDTO);

    List<Product> productList = List.of(testProduct, product2);

    when(productRepository.findAllWithCategory()).thenReturn(productList);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);
    when(productMapper.toDTO(product2)).thenReturn(productDTO2);

    List<ProductDTO> result = productService.getAllProducts();

    assertEquals(2, result.size());
    assertEquals("Test Product", result.get(0).getName());
    assertEquals("Product 2", result.get(1).getName());
  }

  // ==================== addNewProduct ====================

  @Test
  @DisplayName("Should add new product successfully")
  void testAddNewProductSuccess() throws IOException {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn("test-image.jpg");
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ProductDTO result = productService.addNewProduct(productCreateDTO, multipartFile);

    assertNotNull(result);
    assertEquals("Test Product", result.getName());
    assertEquals(new BigDecimal("99.99"), result.getPrice());
    assertTrue(result.getActive());
    verify(categoryRepository, times(1)).findById(1L);
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("Should throw CategoryNotFoundException when category not found")
  void testAddNewProductCategoryNotFound() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class,
        () -> productService.addNewProduct(productCreateDTO, multipartFile));
    verify(categoryRepository, times(1)).findById(1L);
    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when image file is empty")
  void testAddNewProductEmptyImage() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(multipartFile.isEmpty()).thenReturn(true);

    assertThrows(RuntimeException.class,
        () -> productService.addNewProduct(productCreateDTO, multipartFile));
    verify(productRepository, never()).save(any());
  }

  // ==================== updateProduct ====================

  @Test
  @DisplayName("Should update product successfully")
  void testUpdateProductSuccess() {
    Product productToUpdate = new Product();
    productToUpdate.setId(1L);
    productToUpdate.setName("Old Product");
    productToUpdate.setPrice(new BigDecimal("50.00"));
    productToUpdate.setActive(false);
    productToUpdate.setCategory(testCategory);

    when(productRepository.findById(1L)).thenReturn(Optional.of(productToUpdate));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(productRepository.save(any(Product.class))).thenReturn(productToUpdate);
    when(productMapper.toDTO(productToUpdate)).thenReturn(testProductDTO);

    ProductDTO result = productService.updateProduct(1L, productUpdateDTO);

    assertEquals("Updated Product", productToUpdate.getName());
    assertEquals(new BigDecimal("79.99"), productToUpdate.getPrice());
    assertTrue(productToUpdate.getActive());
    assertNotNull(result);
    verify(productRepository, times(1)).findById(1L);
    verify(categoryRepository, times(1)).findById(1L);
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("Should throw ProductNotFoundException when updating non-existent product")
  void testUpdateProductNotFound() {
    when(productRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class,
        () -> productService.updateProduct(1L, productUpdateDTO));
    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw CategoryNotFoundException when updating with invalid category")
  void testUpdateProductCategoryNotFound() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class,
        () -> productService.updateProduct(1L, productUpdateDTO));
    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update product fields correctly")
  void testUpdateProductFieldsCorrectly() {
    Product productToUpdate = new Product();
    productToUpdate.setId(1L);
    productToUpdate.setName("Old Name");
    productToUpdate.setPrice(new BigDecimal("10.00"));
    productToUpdate.setActive(false);
    productToUpdate.setCategory(testCategory);

    when(productRepository.findById(1L)).thenReturn(Optional.of(productToUpdate));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(productMapper.toDTO(any(Product.class))).thenReturn(testProductDTO);

    productService.updateProduct(1L, productUpdateDTO);

    assertEquals("Updated Product", productToUpdate.getName());
    assertEquals(new BigDecimal("79.99"), productToUpdate.getPrice());
    assertTrue(productToUpdate.getActive());
    assertEquals(testCategory, productToUpdate.getCategory());
  }

  // ==================== deleteProduct ====================

  @Test
  @DisplayName("Should delete product successfully")
  void testDeleteProductSuccess() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

    productService.deleteProduct(1L);

    verify(productRepository, times(1)).findById(1L);
    verify(productRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
  void testDeleteProductNotFound() {
    when(productRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class,
        () -> productService.deleteProduct(1L));
    verify(productRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should delete product without photo path")
  void testDeleteProductWithoutPhoto() {
    Product productWithoutPhoto = new Product();
    productWithoutPhoto.setId(1L);
    productWithoutPhoto.setPhotoPath(null);

    when(productRepository.findById(1L)).thenReturn(Optional.of(productWithoutPhoto));

    productService.deleteProduct(1L);

    verify(productRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should delete product with photo path")
  void testDeleteProductWithPhoto() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

    productService.deleteProduct(1L);

    verify(productRepository, times(1)).deleteById(1L);
  }

  // ==================== updateProductStatus ====================

  @Test
  @DisplayName("Should toggle product status from active to inactive")
  void testUpdateProductStatusToInactive() {
    Product productActive = new Product();
    productActive.setId(1L);
    productActive.setActive(true);
    productActive.setCategory(testCategory);

    when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(productActive));
    when(productRepository.save(any(Product.class))).thenReturn(productActive);
    when(productMapper.toDTO(productActive)).thenReturn(testProductDTO);

    ProductDTO result = productService.updateProductStatus(1L);

    assertFalse(productActive.getActive());
    assertNotNull(result);
    verify(productRepository, times(1)).findByIdWithCategory(1L);
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("Should toggle product status from inactive to active")
  void testUpdateProductStatusToActive() {
    Product productInactive = new Product();
    productInactive.setId(1L);
    productInactive.setActive(false);
    productInactive.setCategory(testCategory);

    when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(productInactive));
    when(productRepository.save(any(Product.class))).thenReturn(productInactive);
    when(productMapper.toDTO(productInactive)).thenReturn(testProductDTO);

    ProductDTO result = productService.updateProductStatus(1L);

    assertTrue(productInactive.getActive());
    assertNotNull(result);
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("Should throw ProductNotFoundException when updating status of non-existent product")
  void testUpdateProductStatusNotFound() {
    when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class,
        () -> productService.updateProductStatus(1L));
    verify(productRepository, never()).save(any());
  }

  // ==================== Image handling edge cases ====================

  @Test
  @DisplayName("Should throw exception when saving empty image file")
  void testAddNewProductWithEmptyImageFile() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(multipartFile.isEmpty()).thenReturn(true);

    assertThrows(RuntimeException.class,
        () -> productService.addNewProduct(productCreateDTO, multipartFile));
  }

  // ==================== Integration scenarios ====================

  @Test
  @DisplayName("Should update product without changing category")
  void testUpdateProductSameCategory() {
    Product productToUpdate = new Product();
    productToUpdate.setId(1L);
    productToUpdate.setName("Old Name");
    productToUpdate.setPrice(new BigDecimal("50.00"));
    productToUpdate.setCategory(testCategory);

    when(productRepository.findById(1L)).thenReturn(Optional.of(productToUpdate));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(productRepository.save(any(Product.class))).thenReturn(productToUpdate);
    when(productMapper.toDTO(any(Product.class))).thenReturn(testProductDTO);

    ProductDTO result = productService.updateProduct(1L, productUpdateDTO);

    assertEquals(testCategory, productToUpdate.getCategory());
    assertNotNull(result);
    verify(categoryRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should delete multiple products sequentially")
  void testDeleteMultipleProducts() {
    Product product1 = new Product();
    product1.setId(1L);
    product1.setPhotoPath("/static/images/product1.jpg");

    Product product2 = new Product();
    product2.setId(2L);
    product2.setPhotoPath("/static/images/product2.jpg");

    when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

    productService.deleteProduct(1L);
    productService.deleteProduct(2L);

    verify(productRepository, times(2)).deleteById(any());
  }

  @Test
  @DisplayName("Should toggle product status multiple times")
  void testToggleProductStatusMultipleTimes() {
    Product product = new Product();
    product.setId(1L);
    product.setActive(true);
    product.setCategory(testCategory);

    when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(productMapper.toDTO(any(Product.class))).thenReturn(testProductDTO);

    // First toggle: true -> false
    productService.updateProductStatus(1L);
    assertFalse(product.getActive());

    // Second toggle: false -> true
    productService.updateProductStatus(1L);
    assertTrue(product.getActive());

    verify(productRepository, times(2)).save(any(Product.class));
  }

  @Test
  @DisplayName("Should handle product with all fields populated")
  void testGetAllProductsWithCompleteData() {
    List<Product> productList = List.of(testProduct);

    when(productRepository.findAllWithCategory()).thenReturn(productList);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    List<ProductDTO> result = productService.getAllProducts();

    assertNotNull(result);
    assertEquals(1, result.size());
    ProductDTO dto = result.get(0);
    assertEquals(1L, dto.getId());
    assertEquals("Test Product", dto.getName());
    assertEquals(new BigDecimal("99.99"), dto.getPrice());
    assertEquals(100, dto.getStock());
    assertTrue(dto.getActive());
    assertNotNull(dto.getCategory());
  }
}
