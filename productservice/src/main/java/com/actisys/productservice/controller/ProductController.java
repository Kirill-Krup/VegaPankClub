package com.actisys.productservice.controller;

import com.actisys.productservice.dto.ProductDtos.ProductCreateDTO;
import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.dto.ProductDtos.ProductUpdateDTO;
import com.actisys.productservice.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(final ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("getAllProducts")
  public ResponseEntity<List<ProductDTO>> getAllProducts() {
    List<ProductDTO> allProducts = productService.getAllProducts();
    return ResponseEntity.ok(allProducts);
  }

  @PostMapping("/addNewProduct")
  public ResponseEntity<ProductDTO> addNewProduct(@RequestBody ProductCreateDTO productCreateDTO) {
    ProductDTO dto = productService.addNewProduct(productCreateDTO);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/updateProduct/{id}")
  public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO productUpdateDTO) {
    ProductDTO productDTO = productService.updateProduct(id, productUpdateDTO);
    return ResponseEntity.ok(productDTO);
  }

  @DeleteMapping("/deleteProduct/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }
}
