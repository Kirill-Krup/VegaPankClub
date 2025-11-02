package com.actisys.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class CategoryDTO {
  private final Long id;

  @NotBlank(message = "Category name is required")
  @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
  private final String name;
}