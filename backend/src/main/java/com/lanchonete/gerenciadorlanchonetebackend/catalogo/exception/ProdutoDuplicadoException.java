package com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception;

public class ProdutoDuplicadoException extends RuntimeException {
    public ProdutoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
