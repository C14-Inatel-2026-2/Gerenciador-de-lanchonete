package com.lanchonete.gerenciadorlanchonetebackend.compras.repository;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;

public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
}
