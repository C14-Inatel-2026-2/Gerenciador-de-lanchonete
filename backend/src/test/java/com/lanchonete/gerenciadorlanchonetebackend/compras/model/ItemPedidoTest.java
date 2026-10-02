package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPedidoTest {

    @Test
    void construtorArmazenaDadosECalculaSubtotal() {
        ItemPedido item = new ItemPedido(1L, "Hamburguer", new BigDecimal("12.50"), 2);

        assertEquals(1L, item.getProdutoId());
        assertEquals("Hamburguer", item.getNome());
        assertEquals(new BigDecimal("12.50"), item.getPrecoUnitario());
        assertEquals(2, item.getQuantidade());
        assertEquals(new BigDecimal("25.00"), item.calcularSubtotal());
    }

    @Test
    void construtorRejeitaDadosInvalidos() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(null, "Hamburguer", new BigDecimal("12.50"), 1));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(1L, " ", new BigDecimal("12.50"), 1));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(1L, "Hamburguer", new BigDecimal("-1.00"), 1));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(1L, "Hamburguer", new BigDecimal("12.50"), 0));
    }

    @Test
    void criarAPartirDoItemCarrinhoCopiaDadosDoProduto() {
        Produto produto = new Produto("Hamburguer", "Produto de teste", new BigDecimal("12.50"),
            new Categoria("Lanches"));
        produto.setId(1L);
        ItemCarrinho itemCarrinho = new ItemCarrinho(produto, 2);

        ItemPedido itemPedido = ItemPedido.criarAPartirDoItemCarrinho(itemCarrinho);
        produto.setNome("Nome atualizado");
        produto.setPreco(new BigDecimal("15.00"));

        assertEquals(1L, itemPedido.getProdutoId());
        assertEquals("Hamburguer", itemPedido.getNome());
        assertEquals(new BigDecimal("12.50"), itemPedido.getPrecoUnitario());
        assertEquals(new BigDecimal("25.00"), itemPedido.calcularSubtotal());
    }

    @Test
    void criarAPartirDeItemCarrinhoNuloDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> ItemPedido.criarAPartirDoItemCarrinho(null));
    }
}
