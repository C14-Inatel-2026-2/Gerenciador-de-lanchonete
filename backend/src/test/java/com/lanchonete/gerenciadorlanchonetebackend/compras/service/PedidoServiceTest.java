package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.ProdutoTestFactory;
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
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void criarPedidoComCarrinhoValidoDeveSalvarPedido() {
        CarrinhoCompras carrinho = new CarrinhoCompras();
        carrinho.adicionarItem(novoItem(1L, "Hamburguer", "12.50", 2));

        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Pedido pedido = pedidoService.criarPedido(10L, carrinho);

        assertEquals(10L, pedido.getClienteId());
        assertEquals(new BigDecimal("25.00"), pedido.getTotal());

        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void criarPedidoComClienteInvalidoDeveLancarExcecao() {
        CarrinhoCompras carrinho = new CarrinhoCompras();
        carrinho.adicionarItem(novoItem(1L, "Hamburguer", "12.50", 2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> pedidoService.criarPedido(null, carrinho));

        assertEquals("O identificador do cliente deve ser positivo.", exception.getMessage());
        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void criarPedidoComCarrinhoNuloOuVazioDeveLancarExcecaoSemSalvar() {
        assertThrows(IllegalArgumentException.class, () -> pedidoService.criarPedido(10L, null));
        assertThrows(IllegalArgumentException.class,
            () -> pedidoService.criarPedido(10L, new CarrinhoCompras()));
        verifyNoInteractions(pedidoRepository);
    }

    private ItemCarrinho novoItem(Long produtoId, String nome, String preco, int quantidade) {
        return new ItemCarrinho(ProdutoTestFactory.produto(produtoId, nome, preco), quantidade);
    }
}
