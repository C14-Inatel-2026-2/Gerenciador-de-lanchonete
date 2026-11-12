package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

public final class ValidacoesCompras {

    private ValidacoesCompras() {
    }

    static void validarProdutoId(Long produtoId) {
        if (produtoId == null || produtoId <= 0) {
            throw new IllegalArgumentException("O identificador do produto deve ser positivo.");
        }
    }

    static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
