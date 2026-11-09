package com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto;

import java.math.BigDecimal;

public record ProdutoRequest(
        String nome,
        String descricao,
        BigDecimal preco,
        Long categoriaId
) {
}