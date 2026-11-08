package com.lanchonete.gerenciadorlanchonetebackend.exception;

/** Não foi possível identificar o usuário da requisição. Respondido com HTTP 401. */
public class UsuarioNaoAutenticadoException extends RuntimeException {

    public UsuarioNaoAutenticadoException(String message) {
        super(message);
    }
}
