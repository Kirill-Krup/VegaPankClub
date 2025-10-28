package com.actisys.productservice.exception;

public class CategoryNotFoundException extends RuntimeException {

  public CategoryNotFoundException(Long id) {
    super("Category with id: " + id + " not found");
  }
}
