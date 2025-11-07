package com.actisys.billingservice.exception;

public class SessionNotFoundException extends RuntimeException {

  public SessionNotFoundException(Long id) {
    super("Session with id " + id + " not found");
  }
}
