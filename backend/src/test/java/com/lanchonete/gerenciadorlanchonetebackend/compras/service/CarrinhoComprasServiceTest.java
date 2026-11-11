package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.ProdutoTestFactory;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.CarrinhoCompras;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemCarrinho;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarrinhoComprasServiceTest {

    private final CarrinhoComprasService carrinhoComprasService = new CarrinhoComprasService();

    @Test
    void adicionarItemNoCarrinhoDeveAtualizarQuantidade() {
        CarrinhoCompras carrinho = new CarrinhoCompras();
        ItemCarrinho item = novoItem(1L, "Hamburguer", "12.50", 2);

        carrinhoComprasService.adicionarItem(carrinho, item);

        assertEquals(1, carrinho.obterQuantidadeDeProdutosDiferentes());
        assertEquals(2, carrinho.obterQuantidadeTotalDeItens());
    }

    @Test
    void adicionarItemNoCarrinhoComCarrinhoNuloDeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carrinhoComprasService.adicionarItem(null, novoItem(1L, "Hamburguer", "12.50", 2)));

        assertEquals("O carrinho não pode ser nulo.", exception.getMessage());
    }

    private ItemCarrinho novoItem(Long produtoId, String nome, String preco, int quantidade) {
        return new ItemCarrinho(ProdutoTestFactory.produto(produtoId, nome, preco), quantidade);
    }
}