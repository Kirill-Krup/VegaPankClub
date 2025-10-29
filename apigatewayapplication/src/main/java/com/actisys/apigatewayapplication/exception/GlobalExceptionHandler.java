package com.actisys.apigatewayapplication.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Error occurred in API Gateway at path {}: {}",
                exchange.getRequest().getPath().value(), ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex);
        String message = determineErrorMessage(ex);
        String path = exchange.getRequest().getPath().value();

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                LocalDateTime.now()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(bytes);

            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            String fallbackResponse = String.format(
                    "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                    status.value(),
                    status.getReasonPhrase(),
                    "Error processing request",
                    path
            );
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return HttpStatus.valueOf(rse.getStatusCode().value());
        } else if (ex instanceof GatewayException) {
            GatewayException ge = (GatewayException) ex;
            return ge.getStatus();
        } else if (ex instanceof org.springframework.web.server.MethodNotAllowedException) {
            return HttpStatus.METHOD_NOT_ALLOWED;
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        } else if (ex instanceof io.netty.channel.ConnectTimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientRequestException) {
            return HttpStatus.BAD_GATEWAY;
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
            org.springframework.web.reactive.function.client.WebClientResponseException wcre =
                    (org.springframework.web.reactive.function.client.WebClientResponseException) ex;
            return HttpStatus.valueOf(wcre.getStatusCode().value());
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private String determineErrorMessage(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return rse.getReason() != null ? rse.getReason() : rse.getStatusCode().toString();
        } else if (ex instanceof GatewayException) {
            return ex.getMessage();
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            return "Invalid request parameters or body";
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            return "Request timeout - backend service unavailable";
        } else if (ex instanceof io.netty.channel.ConnectTimeoutException) {
            return "Connection timeout - cannot reach backend service";
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientRequestException) {
            return "Cannot connect to backend service";
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
            org.springframework.web.reactive.function.client.WebClientResponseException wcre =
                    (org.springframework.web.reactive.function.client.WebClientResponseException) ex;
            return "Backend service error: " + wcre.getStatusCode();
        } else if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            // Ограничиваем длину сообщения для безопасности
            return ex.getMessage().length() > 200 ?
                    ex.getMessage().substring(0, 200) + "..." :
                    ex.getMessage();
        } else {
            return "An unexpected error occurred in the gateway";
        }
    }

    // Внутренний класс для представления ошибки
    public static class ErrorResponse {
        private final int status;
        private final String error;
        private final String message;
        private final String path;
        private final LocalDateTime timestamp;

        public ErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
        }

        // Getters
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}