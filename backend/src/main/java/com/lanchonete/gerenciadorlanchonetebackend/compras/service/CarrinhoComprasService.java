package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.CarrinhoCompras;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemCarrinho;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import org.springframework.stereotype.Service;

@Service
public class CarrinhoComprasService {
    private final PedidoRepository pedidoRepository;

    public CarrinhoComprasService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public void adicionarItem(CarrinhoCompras carrinho, ItemCarrinho item) {
        if (carrinho == null) {
            throw new IllegalArgumentException("O carrinho não pode ser nulo.");
        }

        carrinho.adicionarItem(item);
    }

    public Pedido finalizarPedido(Long clienteId, CarrinhoCompras carrinho) {
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
