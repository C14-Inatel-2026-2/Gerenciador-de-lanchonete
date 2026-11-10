package com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto;

import java.util.List;

public record CategoriaCardapioResponse(
        Long id,
        String nome,
        List<ProdutoCardapioResponse> produtos
) {
}
