package com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto;

import java.util.List;

public record CategoriaCardapioResponse(
        String nome,
        List<ProdutoCardapioResponse> produtos
) {
}
