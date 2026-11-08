package com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception;

/** Quantidade pedida maior que o estoque disponível (#56). Respondido com HTTP 409. */
public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String message) {
        super(message);
    }
}
