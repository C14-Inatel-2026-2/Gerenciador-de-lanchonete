package com.lanchonete.gerenciadorlanchonetebackend.exception;

/** Recurso solicitado não existe. Respondido com HTTP 404 pelo GlobalExceptionHandler. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
