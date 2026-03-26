package com.example.linkshortener.exception.handler;

import com.example.linkshortener.dto.ErrorResponse;
import com.example.linkshortener.exception.AliasAlreadyExistException;
import com.example.linkshortener.exception.GenerationShortCodeException;
import com.example.linkshortener.exception.UrlExpiredException;
import com.example.linkshortener.exception.UrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UrlNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder().code("URL_NOT_FOUND").message(ex.getMessage()).build());
    }

    @ExceptionHandler(AliasAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAliasAlreadyExist(AliasAlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder().code("ALIAS_ALREADY_EXIST").message(ex.getMessage()).build());
    }

    @ExceptionHandler(GenerationShortCodeException.class)
    public ResponseEntity<ErrorResponse> handleGenerationFailed(GenerationShortCodeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().code("GENERATION_FAILED").message(ex.getMessage()).build());
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(UrlExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ErrorResponse.builder().code("URL_EXPIRED").message(ex.getMessage()).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder().code("VALIDATION_ERROR").message(message).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().code("INTERNAL_ERROR").message(e.getMessage()).build());
    }

}
