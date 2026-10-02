package com.lanchonete.gerenciadorlanchonetebackend.catalogo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProdutoTest {

    @Test
    void deveCriarProdutoComValoresCorretos(){
        Categoria categoria = new Categoria("Lanches");
        Produto produto = new Produto("X-Burguer", "Delicioso", BigDecimal.valueOf(15), categoria);

        assertEquals("X-Burguer", produto.getNome());
        assertEquals(BigDecimal.valueOf(15), produto.getPreco());
        assertTrue(produto.isDisponivel());
        assertEquals(categoria, produto.getCategoria());
    }

    @Test
    void deveMarcarProdutoComoIndisponivel(){
        Produto produto = new Produto("Suco Natural", "Laranja", BigDecimal.valueOf(8), new Categoria("Bebidas"));

        produto.setDisponivel(false);

        assertFalse(produto.isDisponivel());
    }
}
