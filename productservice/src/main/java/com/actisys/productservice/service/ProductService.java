package com.actisys.productservice.service;

import com.actisys.productservice.dto.ProductDtos.ProductCreateDTO;
import com.actisys.productservice.dto.ProductDtos.ProductDTO;
import com.actisys.productservice.dto.ProductDtos.ProductUpdateDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

  List<ProductDTO> getAllProducts();

  ProductDTO addNewProduct(ProductCreateDTO productCreateDTO, MultipartFile productImage);

  ProductDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO);

  void deleteProduct(Long id);

  ProductDTO updateProductStatus(Long id);
}
