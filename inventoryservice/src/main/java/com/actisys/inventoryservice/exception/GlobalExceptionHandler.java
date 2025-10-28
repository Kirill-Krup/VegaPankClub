package com.actisys.inventoryservice.exception;

import com.actisys.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(RoomNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRoomNotFoundException(
      RoomNotFoundException e) {
    ErrorResponse error = new ErrorResponse("ROOM NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(PcNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePcNotFoundException(
      PcNotFoundException e) {
    ErrorResponse error = new ErrorResponse("PC NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }
}

