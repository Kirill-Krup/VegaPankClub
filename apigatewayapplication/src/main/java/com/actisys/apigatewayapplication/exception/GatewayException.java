package com.actisys.apigatewayapplication.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GatewayException extends RuntimeException {

    private final HttpStatus status;

    public GatewayException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public GatewayException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}