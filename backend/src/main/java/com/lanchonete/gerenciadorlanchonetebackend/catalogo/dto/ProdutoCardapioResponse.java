package com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto;

import java.math.BigDecimal;

public record ProdutoCardapioResponse(
        String nome,
        String descricao,
        BigDecimal preco
) {
}
