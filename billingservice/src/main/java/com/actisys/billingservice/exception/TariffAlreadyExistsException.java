package com.actisys.billingservice.exception;

public class TariffAlreadyExistsException extends RuntimeException {

  public TariffAlreadyExistsException(String name) {
    super("Tariff " + name + " already exists");
  }
}
