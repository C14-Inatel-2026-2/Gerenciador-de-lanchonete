package com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto;

import java.math.BigDecimal;

public record ProdutoCardapioResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco
) {
}
