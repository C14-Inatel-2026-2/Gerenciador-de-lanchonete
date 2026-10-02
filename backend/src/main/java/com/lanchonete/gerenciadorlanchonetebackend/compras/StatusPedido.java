package com.lanchonete.gerenciadorlanchonetebackend.compras;

public enum StatusPedido {
    CRIADO,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    CANCELADO;

    public boolean podeTransitarPara(StatusPedido destino) {
        if (destino == null) {
            return false;
        }
        return switch (this) {
            case CRIADO -> destino == EM_PREPARO || destino == CANCELADO;
            case EM_PREPARO -> destino == PRONTO || destino == CANCELADO;
            case PRONTO -> destino == ENTREGUE;
            case ENTREGUE, CANCELADO -> false; // estados finais
        };
    }
}
