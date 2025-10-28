package com.actisys.inventoryservice.exception;

public class PcNotFoundException extends RuntimeException {

  public PcNotFoundException(Long id) {
    super("Pc with id " + id + " not found");
  }
}
