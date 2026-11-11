package com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception;

public class CategoriaDuplicadaException extends RuntimeException {
    public CategoriaDuplicadaException(String mensagem) {
        super(mensagem);
    }
}
