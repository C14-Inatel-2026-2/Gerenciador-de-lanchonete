package com.lanchonete.gerenciadorlanchonetebackend.compras.dto;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pedido como a API devolve (docs/api.md). Separar o DTO da entidade evita expor
 * detalhes do JPA e deixa o formato do JSON estável mesmo se a entidade mudar.
 */
public record PedidoResponse(
    Long id,
    Long clienteId,
    StatusPedido status,
    BigDecimal total,
    LocalDateTime criadoEm,
    List<ItemPedidoResponse> itens
) {

    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getStatus(),
            pedido.getTotal(),
            pedido.getCriadoEm(),
            pedido.getItens().stream().map(ItemPedidoResponse::de).toList()
        );
    }
}