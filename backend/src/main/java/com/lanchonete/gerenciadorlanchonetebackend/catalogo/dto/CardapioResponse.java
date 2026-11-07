package com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto;

import java.util.List;

public record CardapioResponse(
        List<CategoriaCardapioResponse> categorias,
        String mensagem
) {
}
