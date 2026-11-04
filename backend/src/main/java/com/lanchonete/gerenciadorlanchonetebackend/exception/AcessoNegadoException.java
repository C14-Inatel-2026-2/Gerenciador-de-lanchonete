package com.lanchonete.gerenciadorlanchonetebackend.exception;

/** Usuário identificado, mas sem permissão para a operação. Respondido com HTTP 403. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String message) {
        super(message);
    }
}
