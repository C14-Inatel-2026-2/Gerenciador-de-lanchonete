package com.lanchonete.gerenciadorlanchonetebackend.compras.dto;

import java.util.List;

public record PedidoRequest(List<ItemPedidoRequest> itens) {
}
