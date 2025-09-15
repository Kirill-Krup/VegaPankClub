package com.example.vegapank.Exception;

public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException(String email) {

    super("Email " + email + " already exists");
  }
}
