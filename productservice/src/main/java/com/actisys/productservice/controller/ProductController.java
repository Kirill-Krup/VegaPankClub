package com.actisys.productservice.controller;

import com.actisys.productservice.dto.ProductDtos.ProductCreateDTO;
import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.dto.ProductDtos.ProductUpdateDTO;
import com.actisys.productservice.service.ProductService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(final ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/getAllProducts")
  public ResponseEntity<List<ProductDTO>> getAllProducts() {
    List<ProductDTO> allProducts = productService.getAllProducts();
    return ResponseEntity.ok(allProducts);
  }

  @PostMapping("/createProduct")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ProductDTO> addNewProduct(
      @RequestPart("product") ProductCreateDTO productCreateDTO,
      @RequestPart("image") MultipartFile file) {
    ProductDTO dto = productService.addNewProduct(productCreateDTO, file);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/updateProduct/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO productUpdateDTO) {
    ProductDTO productDTO = productService.updateProduct(id, productUpdateDTO);
    return ResponseEntity.ok(productDTO);
  }

  @PutMapping("/updateAvailableStatus/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ProductDTO> updateAvailableStatus(@PathVariable Long id) {
    ProductDTO dto = productService.updateProductStatus(id);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/deleteProduct/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }
}
