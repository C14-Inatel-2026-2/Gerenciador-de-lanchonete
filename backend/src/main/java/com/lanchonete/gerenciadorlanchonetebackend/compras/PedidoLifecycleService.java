package com.lanchonete.gerenciadorlanchonetebackend.compras;

import org.springframework.stereotype.Service;

@Service
public class PedidoLifecycleService {

    private final PedidoRepository pedidoRepository;

    public PedidoLifecycleService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido avancarParaPreparo(Pedido pedido) {
        return atualizarStatus(pedido, StatusPedido.EM_PREPARO);
    }

    public Pedido marcarComoPronto(Pedido pedido) {
        return atualizarStatus(pedido, StatusPedido.PRONTO);
    }

    public Pedido entregar(Pedido pedido) {
        return atualizarStatus(pedido, StatusPedido.ENTREGUE);
    }

    public Pedido cancelarPedido(Pedido pedido) {
        return atualizarStatus(pedido, StatusPedido.CANCELADO);
    }

    // Único ponto que altera status: valida a transição, altera e persiste.
    public Pedido atualizarStatus(Pedido pedido, StatusPedido novoStatus) {
        if (pedido == null) {
            throw new IllegalArgumentException("O pedido não pode ser nulo.");
        }
        if (novoStatus == null) {
            throw new IllegalArgumentException("O novo status não pode ser nulo.");
        }

        StatusPedido atual = pedido.getStatus();
        if (!atual.podeTransitarPara(novoStatus)) {
            throw new TransicaoStatusInvalidaException(atual, novoStatus);
        }

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }
}
