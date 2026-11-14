package com.lanchonete.gerenciadorlanchonetebackend.admin.dto;

/** Produto disponível com poucas unidades em estoque, para o alerta do painel (#56). */
public record ProdutoEstoqueBaixoResponse(
        Long id,
        String nome,
        int quantidadeEstoque
) {
}
