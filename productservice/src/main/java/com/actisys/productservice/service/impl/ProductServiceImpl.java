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
import com.actisys.productservice.service.ProductPhotoStorageService;
import com.actisys.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "products")
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final CategoryRepository categoryRepository;
  private final ProductPhotoStorageService productPhotoStorageService;


  @Override
  @Cacheable(value = "products", key = "'all'")
  public List<ProductDTO> getAllProducts() {
    List<Product> allProducts = productRepository.findAllWithCategory();
    return allProducts.stream().map(productMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(value = "products", key = "'all'", beforeInvocation = true)
  @CachePut(value = "products", key = "#result.id")
  public ProductDTO addNewProduct(ProductCreateDTO productCreateDTO, MultipartFile productImage) {
    Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException(productCreateDTO.getCategoryId()));

    Product product = new Product();
    product.setName(productCreateDTO.getName());
    product.setPrice(productCreateDTO.getPrice());
    product.setCategory(category);
    product.setStock(productCreateDTO.getStock());
    product.setActive(true);

    Product saved = productRepository.save(product);

    if (!productImage.isEmpty()) {
      String newUrl = productPhotoStorageService.uploadProductPhoto(saved.getId(), productImage);
      saved.setPhotoPath(newUrl);
      saved = productRepository.save(saved);
    }

    return productMapper.toDTO(saved);
  }

  @Override
  @Transactional
  @CacheEvict(value = "products", key = "'all'", beforeInvocation = true)
  @CachePut(value = "products", key = "#id")
  public ProductDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

    product.setName(productUpdateDTO.getName());
    product.setPrice(productUpdateDTO.getPrice());
    product.setActive(productUpdateDTO.getIsAvailable());
    product.setCategory(
            categoryRepository.findById(productUpdateDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(productUpdateDTO.getCategoryId()))
    );
    return productMapper.toDTO(productRepository.save(product));
  }


  @Override
  @Transactional
  @CacheEvict(value = "products", allEntries = true)
  public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

    if (product.getPhotoPath() != null) {
      productPhotoStorageService.deleteProductPhoto(product.getPhotoPath());
    }
    productRepository.deleteById(id);
  }

  @Override
  @Transactional
  @CacheEvict(value = "products", key = "'all'", beforeInvocation = true)
  @CachePut(value = "products", key = "#id")
  public ProductDTO updateProductStatus(Long id) {
    Product product = productRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

    product.setActive(!product.getActive());
    return productMapper.toDTO(productRepository.save(product));
  }

}

