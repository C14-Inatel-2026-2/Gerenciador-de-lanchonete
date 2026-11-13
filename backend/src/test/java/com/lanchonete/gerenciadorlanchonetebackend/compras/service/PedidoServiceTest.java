package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import com.lanchonete.gerenciadorlanchonetebackend.compras.ProdutoTestFactory;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.ItemPedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.PedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.CarrinhoCompras;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemCarrinho;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoService produtoService;

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

    @Test
    void criarPedidoBuscaProdutoEUsaPrecoDoCatalogo() {
        Produto produto = ProdutoTestFactory.produto(1L, "Hamburguer", "12.50");
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PedidoRequest request = new PedidoRequest(List.of(new ItemPedidoRequest(1L, 2)));

        Pedido pedido = pedidoService.criarPedidoComItens(10L, request);

        assertEquals(new BigDecimal("12.50"), pedido.getItens().get(0).getPrecoUnitario());
        assertEquals(new BigDecimal("25.00"), pedido.getTotal());
        verify(produtoService).baixarEstoque(1L, 2);
        verify(produtoRepository).findById(1L);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void criarPedidoAgrupaItensAntesDeBaixarEstoque() {
        Produto produto = ProdutoTestFactory.produto(1L, "Hamburguer", "12.50");
        produto.setQuantidadeEstoque(8);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PedidoRequest request = new PedidoRequest(List.of(
                new ItemPedidoRequest(1L, 2),
                new ItemPedidoRequest(1L, 3)
        ));

        Pedido pedido = pedidoService.criarPedidoComItens(10L, request);

        assertEquals(1, pedido.getItens().size());
        assertEquals(5, pedido.getItens().get(0).getQuantidade());
        verify(produtoService).baixarEstoque(1L, 5);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void criarPedidoComEstoqueInsuficienteNaoSalvaPedido() {
        Produto produto = ProdutoTestFactory.produto(1L, "Hamburguer", "12.50");
        produto.setQuantidadeEstoque(1);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoService.baixarEstoque(1L, 2))
                .thenThrow(new EstoqueInsuficienteException(
                        "Estoque insuficiente: disponível 1, solicitado 2"
                ));
        PedidoRequest request = new PedidoRequest(List.of(new ItemPedidoRequest(1L, 2)));

        EstoqueInsuficienteException exception = assertThrows(
                EstoqueInsuficienteException.class,
                () -> pedidoService.criarPedidoComItens(10L, request)
        );

        assertEquals("Produto Hamburguer: Estoque insuficiente: disponível 1, solicitado 2",
                exception.getMessage());
        verify(produtoService).baixarEstoque(1L, 2);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void criarPedidoComProdutoInexistenteLanca404SemSalvar() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());
        PedidoRequest request = new PedidoRequest(List.of(new ItemPedidoRequest(99L, 1)));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> pedidoService.criarPedidoComItens(10L, request));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void criarPedidoComProdutoIndisponivelLancaErroDeValidacaoSemSalvar() {
        Produto produto = ProdutoTestFactory.produto(1L, "Hamburguer", "12.50");
        produto.setDisponivel(false);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        PedidoRequest request = new PedidoRequest(List.of(new ItemPedidoRequest(1L, 1)));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.criarPedidoComItens(10L, request));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void criarPedidoVazioOuComQuantidadeInvalidaNaoConsultaProdutos() {
        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.criarPedidoComItens(10L, new PedidoRequest(List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.criarPedidoComItens(
                        10L,
                        new PedidoRequest(List.of(new ItemPedidoRequest(1L, 0)))
                ));

        verifyNoInteractions(produtoRepository, pedidoRepository);
    }

    @Test
    void listarPedidosDoClienteUsaOrdenacaoDescendenteDoRepositorio() {
        when(pedidoRepository.findByClienteIdOrderByCriadoEmDesc(10L)).thenReturn(List.of());

        assertEquals(List.of(), pedidoService.listarPedidosDoCliente(10L));

        verify(pedidoRepository).findByClienteIdOrderByCriadoEmDesc(10L);
    }

    private ItemCarrinho novoItem(Long produtoId, String nome, String preco, int quantidade) {
        return new ItemCarrinho(ProdutoTestFactory.produto(produtoId, nome, preco), quantidade);
    }
}
