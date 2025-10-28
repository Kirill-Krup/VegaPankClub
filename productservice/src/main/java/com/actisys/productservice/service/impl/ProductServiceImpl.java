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
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final CategoryRepository categoryRepository;

  @Override
  public List<ProductDTO> getAllProducts() {
    List<Product> allProducts = productRepository.findAll();
    return allProducts.stream().map(productMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  public ProductDTO addNewProduct(ProductCreateDTO productCreateDTO) {
    Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
        .orElseThrow(() -> new CategoryNotFoundException(productCreateDTO.getCategoryId()));
    Product product = new Product();
    product.setName(productCreateDTO.getName());
    product.setPrice(productCreateDTO.getPrice());
    product.setCategory(category);
    return productMapper.toDTO(productRepository.save(product));
  }

  @Override
  public ProductDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO) {
    if(productRepository.existsById(id)) {
      throw new ProductNotFoundException(id);
    }
    Product product = productRepository.findById(id).get();
    product.setName(productUpdateDTO.getName());
    product.setPrice(productUpdateDTO.getPrice());
    product.setCategory(categoryRepository.findById(productUpdateDTO.getCategoryId()).get());
    product.setIsAvailable(productUpdateDTO.getIsAvailable());
    return productMapper.toDTO(productRepository.save(product));
  }

  @Override
  public void deleteProduct(Long id) {
    if(!productRepository.existsById(id)) {
      throw new ProductNotFoundException(id);
    }
    productRepository.deleteById(id);
  }
}
