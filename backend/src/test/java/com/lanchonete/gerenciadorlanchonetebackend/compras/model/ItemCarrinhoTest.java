package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.compras.ProdutoTestFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemCarrinhoTest {

    @Test
    void construtorComProdutoNuloDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> new ItemCarrinho(null, 1));
    }

    @Test
    void construtorComProdutoSemIdentificadorValidoDeveLancarExcecao() {
        Produto produtoSemIdentificador = ProdutoTestFactory.produto(null, "Hamburguer", "12.50");

        assertThrows(IllegalArgumentException.class, () -> new ItemCarrinho(produtoSemIdentificador, 1));

        Produto produtoComIdentificadorZero = ProdutoTestFactory.produto(0L, "Hamburguer", "12.50");
        assertThrows(IllegalArgumentException.class, () -> new ItemCarrinho(produtoComIdentificadorZero, 1));
    }

    @Test
    void construtorComNomeVazioOuPrecoNegativoDeveLancarExcecao() {
        Produto produtoSemNome = ProdutoTestFactory.produto(1L, " ", "12.50");
        Produto produtoComPrecoNegativo = ProdutoTestFactory.produto(2L, "Hamburguer", "-1.00");

        assertThrows(IllegalArgumentException.class, () -> new ItemCarrinho(produtoSemNome, 1));
        assertThrows(IllegalArgumentException.class, () -> new ItemCarrinho(produtoComPrecoNegativo, 1));
    }

    @Test
    void quantidadeDeveSerPositivaAoCriarEAlterarItem() {
        Produto produto = ProdutoTestFactory.produto(1L, "Hamburguer", "12.50");

        assertThrows(IllegalArgumentException.class, () -> new ItemCarrinho(produto, 0));

        ItemCarrinho item = new ItemCarrinho(produto, 2);
        assertThrows(IllegalArgumentException.class, () -> item.alterarQuantidade(-1));
        assertEquals(2, item.getQuantidade());
    }

    @Test
    void aumentarEDiminuirQuantidadeAtualizaQuantidadeESubtotal() {
        ItemCarrinho item = new ItemCarrinho(ProdutoTestFactory.produto(1L, "Hamburguer", "12.50"), 2);

        item.aumentarQuantidade(3);
        assertEquals(5, item.getQuantidade());
        assertEquals(new BigDecimal("62.50"), item.calcularSubtotal());

        item.diminuirQuantidade(2);
        assertEquals(3, item.getQuantidade());
        assertEquals(new BigDecimal("37.50"), item.calcularSubtotal());
    }

    @Test
    void diminuirQuantidadeIgualOuMaiorQueAtualDeveLancarExcecao() {
        ItemCarrinho item = new ItemCarrinho(ProdutoTestFactory.produto(1L, "Hamburguer", "12.50"), 2);

        assertThrows(IllegalArgumentException.class, () -> item.diminuirQuantidade(2));
        assertThrows(IllegalArgumentException.class, () -> item.diminuirQuantidade(3));
        assertEquals(2, item.getQuantidade());
    }

    @Test
    void igualdadeDeveConsiderarIdentificadorDoProduto() {
        ItemCarrinho primeiro = new ItemCarrinho(ProdutoTestFactory.produto(1L, "Hamburguer", "12.50"), 2);
        ItemCarrinho mesmoProduto = new ItemCarrinho(ProdutoTestFactory.produto(1L, "Outro nome", "9.00"), 1);
        ItemCarrinho outroProduto = new ItemCarrinho(ProdutoTestFactory.produto(2L, "Batata", "6.00"), 1);

        assertEquals(primeiro, mesmoProduto);
        assertEquals(primeiro.hashCode(), mesmoProduto.hashCode());
        assertNotEquals(primeiro, outroProduto);
    }
}
