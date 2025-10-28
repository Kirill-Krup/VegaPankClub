package com.actisys.productservice.exception;

import com.actisys.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e) {
    ErrorResponse error = new ErrorResponse("ORDER NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }
  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(CategoryNotFoundException e) {
    ErrorResponse error = new ErrorResponse("CATEGORY NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }
  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException e) {
    ErrorResponse error = new ErrorResponse("PRODUCT NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }
}

