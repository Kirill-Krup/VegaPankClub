package com.actisys.productservice.exception;

public class CategoryAlreadyException extends RuntimeException {

  public CategoryAlreadyException(String name) {
    super("Category with name: " + name + " already exists");
  }
}
