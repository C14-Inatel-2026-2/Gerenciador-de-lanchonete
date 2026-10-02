package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.CarrinhoCompras;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemCarrinho;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoComprasServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private CarrinhoComprasService carrinhoComprasService;

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

    @Test
    void finalizarPedidoValidoDeveSalvarPedidoNoRepositorio() {
        CarrinhoCompras carrinho = new CarrinhoCompras();
        carrinho.adicionarItem(novoItem(1L, "Hamburguer", "12.50", 2));
        Pedido pedidoEsperado = carrinho.gerarPedido(10L);
        when(pedidoRepository.salvar(any(Pedido.class))).thenReturn(pedidoEsperado);

        Pedido pedido = carrinhoComprasService.finalizarPedido(10L, carrinho);

        assertEquals(10L, pedido.getClienteId());
        assertEquals(new BigDecimal("25.00"), pedido.getTotal());
        verify(pedidoRepository).salvar(any(Pedido.class));
    }

    @Test
    void finalizarPedidoComCarrinhoVazioNaoDeveChamarRepositorio() {
        CarrinhoCompras carrinho = new CarrinhoCompras();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carrinhoComprasService.finalizarPedido(10L, carrinho));

        assertEquals("Não é possível criar um pedido com carrinho vazio.", exception.getMessage());
        verifyNoInteractions(pedidoRepository);
    }

    private ItemCarrinho novoItem(Long produtoId, String nome, String preco, int quantidade) {
        Produto produto = new Produto(nome, "Produto de teste", new BigDecimal(preco), new Categoria("Lanches"));
        produto.setId(produtoId);
        return new ItemCarrinho(produto, quantidade);
    }
}
