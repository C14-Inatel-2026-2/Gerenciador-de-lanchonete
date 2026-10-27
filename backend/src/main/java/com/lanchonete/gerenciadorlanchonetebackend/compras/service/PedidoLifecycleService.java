package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
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

    public Pedido atualizarStatus(Pedido pedido, StatusPedido novoStatus) {
        if (pedido == null) {
            throw new IllegalArgumentException("O pedido não pode ser nulo.");
        }
        pedido.alterarStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }
}
