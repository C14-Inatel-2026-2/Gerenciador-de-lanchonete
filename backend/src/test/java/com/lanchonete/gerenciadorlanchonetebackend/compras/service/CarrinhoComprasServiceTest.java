package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
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
        Produto produto = new Produto(nome, "Produto de teste", new BigDecimal(preco), new Categoria("Lanches"));
        produto.setId(produtoId);
        return new ItemCarrinho(produto, quantidade);
    }
}