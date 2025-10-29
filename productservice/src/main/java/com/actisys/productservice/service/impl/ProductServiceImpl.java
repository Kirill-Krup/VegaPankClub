package com.actisys.productservice.service.impl;

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
import com.actisys.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "products")
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final CategoryRepository categoryRepository;

  public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper,
      CategoryRepository categoryRepository) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
    this.categoryRepository = categoryRepository;
  }

  @Override
  @Cacheable(value = "products", key = "'all'")
  public List<ProductDTO> getAllProducts() {
    List<Product> allProducts = productRepository.findAll();
    return allProducts.stream().map(productMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  @CachePut(value = "products", key = "#result.id")
  @CacheEvict(value = "products", key = "'all'", beforeInvocation = true)
  public ProductDTO addNewProduct(ProductCreateDTO productCreateDTO) {
    Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
        .orElseThrow(() -> new CategoryNotFoundException(productCreateDTO.getCategoryId()));

    Product product = new Product();
    product.setName(productCreateDTO.getName());
    product.setPrice(productCreateDTO.getPrice());
    product.setCategory(category);
    product.setIsAvailable(true);

    return productMapper.toDTO(productRepository.save(product));
  }

  @Override
  @CachePut(value = "products", key = "#id")
  @CacheEvict(value = "products", key = "'all'", beforeInvocation = true)
  public ProductDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO) {
    if (!productRepository.existsById(id)) {
      throw new ProductNotFoundException(id);
    }

    Product product = productRepository.findById(id).get();
    product.setName(productUpdateDTO.getName());
    product.setPrice(productUpdateDTO.getPrice());
    product.setIsAvailable(productUpdateDTO.getIsAvailable());
    product.setCategory(
        categoryRepository.findById(productUpdateDTO.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException(productUpdateDTO.getCategoryId()))
    );

    return productMapper.toDTO(productRepository.save(product));
  }

  @Override
  @CacheEvict(value = "products", allEntries = true)
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ProductNotFoundException(id);
    }
    productRepository.deleteById(id);
  }
}
