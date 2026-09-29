package com.lanchonete.gerenciadorlanchonetebackend.compras;

import org.springframework.stereotype.Service;

@Service
public class PedidoLifecycleService {

    private final PedidoRepository pedidoRepository;

    public PedidoLifecycleService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public void avancarParaPreparo(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.CRIADO) {
            throw new IllegalStateException("Apenas pedidos CRIADOS podem ir para preparo.");
        }
        pedido.setStatus(StatusPedido.EM_PREPARO);
    }

    public Pedido atualizarStatusNoBanco(Pedido pedido, StatusPedido novoStatus) {
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    public Pedido cancelarPedido(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.PRONTO || pedido.getStatus() == StatusPedido.ENTREGUE) {
            throw new IllegalStateException("Não é possível cancelar um pedido que já está PRONTO ou ENTREGUE.");
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }
}