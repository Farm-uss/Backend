package com.example.practice.common.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handle(AppException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("message", e.getMessage()));
    }
}
