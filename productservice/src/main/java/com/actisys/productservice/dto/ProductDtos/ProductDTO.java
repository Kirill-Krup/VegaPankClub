package com.actisys.productservice.dto.ProductDtos;

import com.actisys.productservice.dto.CategoryDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class ProductDTO {
  private final Long productId;

  @NotBlank(message = "Product name is required")
  @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
  private final String name;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  private final BigDecimal price;

  @NotNull(message = "Category is required")
  private final CategoryDTO category;

  @NotNull(message = "Availability status is required")
  private final Boolean isAvailable;
}