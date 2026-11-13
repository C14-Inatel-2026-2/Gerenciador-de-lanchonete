package com.lanchonete.gerenciadorlanchonetebackend.admin.dto;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Resumo do dia para o painel administrativo (US05 · #48, docs/api.md).
 *
 * "Hoje" é o dia no fuso de Brasília. pedidosHoje conta todos os pedidos do dia,
 * inclusive os cancelados; faturamentoHoje e ticketMedio desconsideram os cancelados.
 */
public record ResumoResponse(
        long pedidosHoje,
        BigDecimal faturamentoHoje,
        BigDecimal ticketMedio,
        Map<StatusPedido, Long> pedidosPorStatus,
        long produtosAtivos,
        long usuariosCadastrados,
        List<PedidoRecenteResponse> pedidosRecentes
) {
}
