package com.actisys.billingservice.exception;

public class TariffNotFoundException extends RuntimeException {

  public TariffNotFoundException(Long id) {
    super("Tariff with id " + id + " not found");
  }
}
