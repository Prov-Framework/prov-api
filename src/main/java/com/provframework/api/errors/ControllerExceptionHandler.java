package com.provframework.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.HashMap;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleUserNotFound(BadRequestException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("message", ex.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
