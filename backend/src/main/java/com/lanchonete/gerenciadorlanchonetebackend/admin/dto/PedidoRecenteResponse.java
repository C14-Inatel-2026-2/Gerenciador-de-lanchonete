package com.lanchonete.gerenciadorlanchonetebackend.admin.dto;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha da tabela de pedidos recentes do painel: já traz o nome do cliente. */
public record PedidoRecenteResponse(
        Long id,
        String cliente,
        StatusPedido status,
        BigDecimal total,
        LocalDateTime criadoEm
) {
}
