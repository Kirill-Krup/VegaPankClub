package com.actisys.userservice.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long userId) {
        super("User don't have sufficient funds");
    }
}
