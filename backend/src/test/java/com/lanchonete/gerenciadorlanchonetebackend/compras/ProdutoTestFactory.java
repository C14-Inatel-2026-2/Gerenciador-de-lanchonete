package com.lanchonete.gerenciadorlanchonetebackend.compras;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;

import java.math.BigDecimal;

public final class ProdutoTestFactory {

    private ProdutoTestFactory() {
    }

    public static Produto produto(Long id, String nome, String preco) {
        Produto produto = new Produto(nome, "Produto de teste", new BigDecimal(preco), new Categoria("Lanches"));
        produto.setId(id);
        return produto;
    }
}
