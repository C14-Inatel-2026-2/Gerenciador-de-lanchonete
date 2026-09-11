package com.lanchonete.gerenciadorlanchonetebackend.user.exception;

public class UserValidationException extends RuntimeException {

    public UserValidationException(String message) {
        super(message);
    }
}