package com.actisys.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryDTO {
  private final Long categoryId;

  @NotBlank(message = "Category name is required")
  @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
  private final String name;
}