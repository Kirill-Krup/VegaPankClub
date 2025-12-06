package com.actisys.billingservice.exception;

import com.actisys.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(TariffNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTariffNotFoundException(TariffNotFoundException e) {
    ErrorResponse error = new ErrorResponse("TARIFF NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(TariffAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleTariffAlreadyExistsException(TariffAlreadyExistsException e) {
    ErrorResponse error = new ErrorResponse("TARIFF ALREADY EXISTS", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(SessionNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSessionNotFoundException(SessionNotFoundException e) {
    ErrorResponse error = new ErrorResponse("SESSION NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e) {
    ErrorResponse error = new ErrorResponse("ORDER NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }
}

