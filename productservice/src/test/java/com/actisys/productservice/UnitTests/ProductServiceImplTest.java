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
import com.actisys.productservice.service.ProductPhotoStorageService;
import com.actisys.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
  private ProductPhotoStorageService productPhotoStorageService;

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
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Electronics");

    testCategoryDTO = CategoryDTO.builder()
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

  // ==================== addNewProduct ====================

  @Test
  @DisplayName("Should add new product successfully without image")
  void testAddNewProductSuccessWithoutImage() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(multipartFile.isEmpty()).thenReturn(true);
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ProductDTO result = productService.addNewProduct(productCreateDTO, multipartFile);

    assertNotNull(result);
    assertEquals("Test Product", result.getName());
    verify(categoryRepository, times(1)).findById(1L);
    verify(productRepository, times(1)).save(any(Product.class));
    verify(productPhotoStorageService, never()).uploadProductPhoto(anyLong(), any());
  }

  @Test
  @DisplayName("Should add new product successfully with image")
  void testAddNewProductSuccessWithImage() {
    String photoUrl = "/static/images/new-product.jpg";

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(multipartFile.isEmpty()).thenReturn(false);
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
    when(productPhotoStorageService.uploadProductPhoto(anyLong(), any())).thenReturn(photoUrl);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ProductDTO result = productService.addNewProduct(productCreateDTO, multipartFile);

    assertNotNull(result);
    assertEquals("Test Product", result.getName());
    verify(categoryRepository, times(1)).findById(1L);
    verify(productRepository, times(2)).save(any(Product.class)); // Первое сохранение + после загрузки фото
    verify(productPhotoStorageService, times(1)).uploadProductPhoto(anyLong(), eq(multipartFile));
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
    verify(productRepository, times(1)).save(productToUpdate);
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

  // ==================== deleteProduct ====================

  @Test
  @DisplayName("Should delete product successfully with photo")
  void testDeleteProductSuccessWithPhoto() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

    productService.deleteProduct(1L);

    verify(productRepository, times(1)).findById(1L);
    verify(productPhotoStorageService, times(1)).deleteProductPhoto("/static/images/test-image.jpg");
    verify(productRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should delete product successfully without photo")
  void testDeleteProductSuccessWithoutPhoto() {
    Product productWithoutPhoto = new Product();
    productWithoutPhoto.setId(1L);
    productWithoutPhoto.setPhotoPath(null);

    when(productRepository.findById(1L)).thenReturn(Optional.of(productWithoutPhoto));

    productService.deleteProduct(1L);

    verify(productRepository, times(1)).findById(1L);
    verify(productPhotoStorageService, never()).deleteProductPhoto(any());
    verify(productRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
  void testDeleteProductNotFound() {
    when(productRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class,
        () -> productService.deleteProduct(1L));
    verify(productRepository, never()).deleteById(any());
    verify(productPhotoStorageService, never()).deleteProductPhoto(any());
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
    verify(productRepository, times(1)).save(productActive);
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
    verify(productRepository, times(1)).save(productInactive);
  }

  @Test
  @DisplayName("Should throw ProductNotFoundException when updating status of non-existent product")
  void testUpdateProductStatusNotFound() {
    when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class,
        () -> productService.updateProductStatus(1L));
    verify(productRepository, never()).save(any());
  }

  // ==================== Edge Cases ====================

  @Test
  @DisplayName("Should handle product creation with empty image file")
  void testAddNewProductWithEmptyImage() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(multipartFile.isEmpty()).thenReturn(true);
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
    when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

    ProductDTO result = productService.addNewProduct(productCreateDTO, multipartFile);

    assertNotNull(result);
    verify(productPhotoStorageService, never()).uploadProductPhoto(anyLong(), any());
  }

  @Test
  @DisplayName("Should update product and save photo path when image uploaded")
  void testAddNewProductImageUploadFlow() {
    String expectedPhotoUrl = "/static/images/1-product.jpg";
    Product savedProduct = new Product();
    savedProduct.setId(1L);
    savedProduct.setName("New Product");
    savedProduct.setPhotoPath(null);

    Product updatedProduct = new Product();
    updatedProduct.setId(1L);
    updatedProduct.setName("New Product");
    updatedProduct.setPhotoPath(expectedPhotoUrl);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
    when(multipartFile.isEmpty()).thenReturn(false);
    when(productRepository.save(any(Product.class)))
        .thenReturn(savedProduct)  // Первое сохранение без фото
        .thenReturn(updatedProduct); // Второе сохранение с фото
    when(productPhotoStorageService.uploadProductPhoto(1L, multipartFile)).thenReturn(expectedPhotoUrl);
    when(productMapper.toDTO(updatedProduct)).thenReturn(testProductDTO);

    ProductDTO result = productService.addNewProduct(productCreateDTO, multipartFile);

    assertNotNull(result);
    verify(productRepository, times(2)).save(any(Product.class));
    verify(productPhotoStorageService, times(1)).uploadProductPhoto(1L, multipartFile);
  }

  @Test
  @DisplayName("Should delete multiple products with different photo scenarios")
  void testDeleteMultipleProducts() {
    Product productWithPhoto = new Product();
    productWithPhoto.setId(1L);
    productWithPhoto.setPhotoPath("/static/images/product1.jpg");

    Product productWithoutPhoto = new Product();
    productWithoutPhoto.setId(2L);
    productWithoutPhoto.setPhotoPath(null);

    when(productRepository.findById(1L)).thenReturn(Optional.of(productWithPhoto));
    when(productRepository.findById(2L)).thenReturn(Optional.of(productWithoutPhoto));

    productService.deleteProduct(1L);
    productService.deleteProduct(2L);

    verify(productPhotoStorageService, times(1)).deleteProductPhoto("/static/images/product1.jpg");
    verify(productPhotoStorageService, never()).deleteProductPhoto(null);
    verify(productRepository, times(2)).deleteById(any());
  }
}