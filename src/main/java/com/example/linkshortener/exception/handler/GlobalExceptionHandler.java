package com.example.linkshortener.exception.handler;

import com.example.linkshortener.dto.ErrorResponse;
import com.example.linkshortener.dto.FieldErrorDto;
import com.example.linkshortener.exception.AliasAlreadyExistException;
import com.example.linkshortener.exception.GenerationShortCodeException;
import com.example.linkshortener.exception.UrlExpiredException;
import com.example.linkshortener.exception.UrlNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UrlNotFoundException ex) {
        log.warn("URL not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder().code("URL_NOT_FOUND").message(ex.getMessage()).build());
    }

    @ExceptionHandler(AliasAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAliasAlreadyExist(AliasAlreadyExistException ex) {
        log.warn("Alias already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder().code("ALIAS_ALREADY_EXIST").message(ex.getMessage()).build());
    }

    @ExceptionHandler(GenerationShortCodeException.class)
    public ResponseEntity<ErrorResponse> handleGenerationFailed(GenerationShortCodeException ex) {
        log.error("Short code generation failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().code("GENERATION_FAILED").message(ex.getMessage()).build());
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(UrlExpiredException ex) {
        log.warn("URL expired: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ErrorResponse.builder().code("URL_EXPIRED").message(ex.getMessage()).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorDto> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new FieldErrorDto(e.getField(), e.getDefaultMessage()))
                .toList();

        log.error("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("VALIDATION_ERROR")
                        .message("Validation failed")
                        .errors(errors)
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .code("DATA_INTEGRITY_ERROR")
                        .message("Data integrity violation")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code("INTERNAL_ERROR")
                        .message("Internal server error")
                        .build());
    }

}
