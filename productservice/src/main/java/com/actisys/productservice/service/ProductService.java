package com.actisys.productservice.service;

import com.actisys.productservice.dto.ProductDtos.ProductCreateDTO;
import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.dto.ProductDtos.ProductUpdateDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

  /**
   * Retrieves all products with category information from cache.
   * Uses "all" key for list caching to optimize repeated admin panel requests.
   *
   * @return list of all active and inactive products as DTOs
   */
  List<ProductDTO> getAllProducts();

  /**
   * Creates new product with category validation and optional photo upload.
   * Uploads photo to S3 storage if provided, updates cache before/after operation.
   *
   * @param productCreateDTO product data including category ID and stock
   * @param productImage optional product photo file
   * @return DTO of created product with photo URL if uploaded
   */
  ProductDTO addNewProduct(ProductCreateDTO productCreateDTO, MultipartFile productImage);

  /**
   * Updates existing product details including name, price, availability and category.
   * Validates category existence, updates cache entry for specific product ID.
   *
   * @param id product identifier to update
   * @param productUpdateDTO updated product data
   * @return updated ProductDTO with new values
   */
  ProductDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO);

  /**
   * Permanently deletes product and its associated photo from S3 storage.
   * Clears all products cache entries to maintain consistency across services.
   *
   * @param id product identifier to delete
   */
  void deleteProduct(Long id);

  /**
   * Toggles product availability status between active and inactive states.
   * Updates cache entry for specific product while invalidating "all" list cache.
   *
   * @param id product identifier to toggle status
   * @return updated ProductDTO with toggled active flag
   */
  ProductDTO updateProductStatus(Long id);
}
