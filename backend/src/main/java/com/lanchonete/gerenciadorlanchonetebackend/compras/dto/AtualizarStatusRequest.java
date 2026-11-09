package com.lanchonete.gerenciadorlanchonetebackend.compras.dto;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;

/**
 * Corpo do PATCH /api/pedidos/{id}/status: { "status": "EM_PREPARO" }.
 * Um valor fora do enum faz o Jackson falhar ao ler o JSON, e a API responde 400.
 */
public record AtualizarStatusRequest(StatusPedido status) {
}