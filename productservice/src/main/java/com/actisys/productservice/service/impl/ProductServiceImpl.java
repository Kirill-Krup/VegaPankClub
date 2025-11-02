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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@CacheConfig(cacheNames = "products")
public class ProductServiceImpl implements ProductService {

  @Value("${app.upload-dir}")
  private String uploadDir;

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

    String photoPath = saveProductImage(productImage);
    Product product = new Product();
    product.setName(productCreateDTO.getName());
    product.setPrice(productCreateDTO.getPrice());
    product.setCategory(category);
    product.setPhotoPath(photoPath);
    product.setStock(productCreateDTO.getStock());
    product.setActive(true);

    Product saved = productRepository.save(product);

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
      deleteProductImage(product.getPhotoPath());
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

  private String saveProductImage(MultipartFile imageFile) {
    try {
      if (imageFile.isEmpty()) {
        throw new RuntimeException("Image file is empty");
      }
      String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

      String userDir = System.getProperty("user.dir");
      Path uploadPath = Paths.get(userDir, "productservice", uploadDir);

      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      Path filePath = uploadPath.resolve(filename);
      imageFile.transferTo(filePath.toFile());

      System.out.println("File saved to: " + filePath.toAbsolutePath());
      return "/static/images/" + filename;
    } catch (IOException e) {
      throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
    }
  }

  private void deleteProductImage(String photoPath) {
    try {
      String filename = photoPath.replace("/static/images/", "");

      String userDir = System.getProperty("user.dir");
      Path uploadPath = Paths.get(userDir, "productservice", uploadDir);
      Path filePath = uploadPath.resolve(filename);

      if (Files.exists(filePath)) {
        Files.delete(filePath);
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}

