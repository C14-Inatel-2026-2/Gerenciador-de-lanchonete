package com.lanchonete.gerenciadorlanchonetebackend.compras;

public class TransicaoStatusInvalidaException extends IllegalStateException {

    public TransicaoStatusInvalidaException(StatusPedido atual, StatusPedido destino) {
        super("Transição de status inválida: " + atual + " -> " + destino + ".");
    }
}
