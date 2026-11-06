package com.lanchonete.gerenciadorlanchonetebackend.compras.dto;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;

import java.math.BigDecimal;

/** Item de um pedido como a API devolve (docs/api.md). O subtotal já vem calculado. */
public record ItemPedidoResponse(
    Long produtoId,
    String nome,
    BigDecimal precoUnitario,
    int quantidade,
    BigDecimal subtotal
) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(
            item.getProdutoId(),
            item.getNome(),
            item.getPrecoUnitario(),
            item.getQuantidade(),
            item.calcularSubtotal()
        );
    }
}