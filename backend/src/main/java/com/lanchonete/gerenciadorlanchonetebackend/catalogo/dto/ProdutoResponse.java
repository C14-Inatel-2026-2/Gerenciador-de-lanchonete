package com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        boolean disponivel,
        Long categoriaId,
        int quantidadeEstoque
) {
}