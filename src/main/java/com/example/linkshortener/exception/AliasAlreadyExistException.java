package com.example.linkshortener.exception;

public class AliasAlreadyExistException extends RuntimeException {
    public AliasAlreadyExistException(String message) {
        super(message);
    }
}
