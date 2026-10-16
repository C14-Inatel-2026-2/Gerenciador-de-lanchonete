package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

public class TransicaoStatusInvalidaException extends IllegalStateException {

    public TransicaoStatusInvalidaException(StatusPedido atual, StatusPedido destino) {
        super("Transição de status inválida: " + atual + " -> " + destino + ".");
    }
}
