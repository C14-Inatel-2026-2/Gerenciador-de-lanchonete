package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.CarrinhoCompras;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido criarPedido(Long clienteId, CarrinhoCompras carrinho) {
        if (clienteId == null || clienteId <= 0) {
            throw new IllegalArgumentException("O identificador do cliente deve ser positivo.");
        }

        if (carrinho == null || carrinho.estaVazio()) {
            throw new IllegalArgumentException("Não é possível criar um pedido com carrinho vazio.");
        }

        Pedido pedido = carrinho.gerarPedido(clienteId);
        return pedidoRepository.salvar(pedido);
    }
}
