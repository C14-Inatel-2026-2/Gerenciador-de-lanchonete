package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoTest {

    @Test
    void construtorCriaPedidoComTotalStatusEItensImutaveis() {
        ItemPedido item = new ItemPedido(1L, "Hamburguer", new BigDecimal("12.50"), 2);
        Pedido pedido = new Pedido(10L, List.of(item));

        assertEquals(10L, pedido.getClienteId());
        assertEquals(List.of(item), pedido.getItens());
        assertEquals(new BigDecimal("25.00"), pedido.getTotal());
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
        assertNotNull(pedido.getCriadoEm());
        assertThrows(UnsupportedOperationException.class, () -> pedido.getItens().clear());
    }

    @Test
    void construtorRejeitaClienteOuListaDeItensInvalidos() {
        ItemPedido item = new ItemPedido(1L, "Hamburguer", new BigDecimal("12.50"), 1);

        assertThrows(IllegalArgumentException.class, () -> new Pedido(null, List.of(item)));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(0L, List.of(item)));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(10L, null));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(10L, List.of()));
    }

    @Test
    void criarAPartirDoCarrinhoConverteItensECalculaTotal() {
        CarrinhoCompras carrinho = new CarrinhoCompras();
        carrinho.adicionarItem(novoItem(1L, "Hamburguer", "12.50", 2));
        carrinho.adicionarItem(novoItem(2L, "Batata", "6.00", 1));

        Pedido pedido = Pedido.criarAPartirDoCarrinho(10L, carrinho);

        assertEquals(10L, pedido.getClienteId());
        assertEquals(2, pedido.getItens().size());
        assertEquals(new BigDecimal("31.00"), pedido.getTotal());
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
    }

    @Test
    void criarAPartirDeCarrinhoNuloOuVazioDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> Pedido.criarAPartirDoCarrinho(10L, null));
        assertThrows(IllegalArgumentException.class,
            () -> Pedido.criarAPartirDoCarrinho(10L, new CarrinhoCompras()));
    }

    private ItemCarrinho novoItem(Long produtoId, String nome, String preco, int quantidade) {
        Produto produto = new Produto(nome, "Produto de teste", new BigDecimal(preco), new Categoria("Lanches"));
        produto.setId(produtoId);
        return new ItemCarrinho(produto, quantidade);
    }
}
