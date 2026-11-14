package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.TransicaoStatusInvalidaException;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoLifecycleServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private PedidoLifecycleService lifecycleService;

    private Pedido pedidoBase;

    @BeforeEach
    void setUp() {
        ItemPedido item = new ItemPedido(1L, "Hamburguer", BigDecimal.valueOf(25), 1);
        pedidoBase = new Pedido(100L, List.of(item));
        pedidoBase.setId(1L);
    }

    @Test
    void deveAvancarParaPreparoESalvarNoBanco() {
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido atualizado = lifecycleService.atualizarStatus(pedidoBase, StatusPedido.EM_PREPARO);

        assertEquals(StatusPedido.EM_PREPARO, atualizado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void deveCancelarPedidoCriadoESalvarNoBanco() {
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = lifecycleService.atualizarStatus(pedidoBase, StatusPedido.CANCELADO);

        assertEquals(StatusPedido.CANCELADO, cancelado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void deveRejeitarTransicaoInvalidaNoMetodoGenerico() {
        // CRIADO -> ENTREGUE pularia etapas
        TransicaoStatusInvalidaException excecao = assertThrows(
            TransicaoStatusInvalidaException.class,
            () -> lifecycleService.atualizarStatus(pedidoBase, StatusPedido.ENTREGUE));

        assertEquals("Transição de status inválida: CRIADO -> ENTREGUE.", excecao.getMessage());
        assertEquals(StatusPedido.CRIADO, pedidoBase.getStatus());
        verify(pedidoRepository, never()).save(any());
    }

    @ParameterizedTest(name = "não pode cancelar pedido {0}")
    @EnumSource(value = StatusPedido.class, names = {"PRONTO", "ENTREGUE", "CANCELADO"})
    void naoDeveCancelarPedidoEmEstadoQueNaoPermite(StatusPedido statusAtual) {
        levarPedidoAte(statusAtual);

        assertThrows(TransicaoStatusInvalidaException.class,
            () -> lifecycleService.atualizarStatus(pedidoBase, StatusPedido.CANCELADO));

        assertEquals(statusAtual, pedidoBase.getStatus());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void naoDeveAceitarPedidoNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> lifecycleService.atualizarStatus(null, StatusPedido.EM_PREPARO));
        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void naoDeveAceitarNovoStatusNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> lifecycleService.atualizarStatus(pedidoBase, null));
        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void deveMarcarComoProntoPedidoEmPreparo() {
        levarPedidoAte(StatusPedido.EM_PREPARO);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido atualizado = lifecycleService.atualizarStatus(pedidoBase, StatusPedido.PRONTO);

        assertEquals(StatusPedido.PRONTO, atualizado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void deveEntregarPedidoPronto() {
        levarPedidoAte(StatusPedido.PRONTO);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido atualizado = lifecycleService.atualizarStatus(pedidoBase, StatusPedido.ENTREGUE);

        assertEquals(StatusPedido.ENTREGUE, atualizado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void deveCancelarPedidoEmPreparo() {
        levarPedidoAte(StatusPedido.EM_PREPARO);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = lifecycleService.atualizarStatus(pedidoBase, StatusPedido.CANCELADO);

        assertEquals(StatusPedido.CANCELADO, cancelado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void listarFilaBuscaSoStatusDaCozinhaDoMaisAntigoParaOMaisNovo() {
        when(pedidoRepository.findByStatusInOrderByCriadoEmAsc(
                List.of(StatusPedido.CRIADO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO)))
            .thenReturn(List.of(pedidoBase));

        List<Pedido> fila = lifecycleService.listarFila();

        assertEquals(List.of(pedidoBase), fila);
    }

    @Test
    void atualizarStatusPorIdBuscaPedidoESalvaNovoStatus() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoBase));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido atualizado = lifecycleService.atualizarStatusPorId(1L, StatusPedido.EM_PREPARO);

        assertEquals(StatusPedido.EM_PREPARO, atualizado.getStatus());
        verify(pedidoRepository).save(pedidoBase);
    }

    @Test
    void atualizarStatusPorIdDePedidoInexistenteLanca404SemSalvar() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
            () -> lifecycleService.atualizarStatusPorId(99L, StatusPedido.EM_PREPARO));

        assertEquals("Pedido não encontrado", excecao.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    // ---- devolução de estoque ao cancelar (#56)

    @Test
    void cancelarDevolveAoEstoqueAQuantidadeDeCadaItem() {
        Pedido pedido = new Pedido(100L, List.of(
            new ItemPedido(1L, "Hamburguer", BigDecimal.valueOf(25), 2),
            new ItemPedido(2L, "Suco", BigDecimal.valueOf(8), 3)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        lifecycleService.atualizarStatus(pedido, StatusPedido.CANCELADO);

        verify(produtoService).reporEstoque(1L, 2);
        verify(produtoService).reporEstoque(2L, 3);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void entregarNaoDevolveEstoque() {
        levarPedidoAte(StatusPedido.PRONTO);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        lifecycleService.atualizarStatus(pedidoBase, StatusPedido.ENTREGUE);

        verifyNoInteractions(produtoService);
    }

    @Test
    void cancelamentoRecusadoNaoDevolveEstoque() {
        levarPedidoAte(StatusPedido.PRONTO);

        assertThrows(TransicaoStatusInvalidaException.class,
            () -> lifecycleService.atualizarStatus(pedidoBase, StatusPedido.CANCELADO));

        verifyNoInteractions(produtoService);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void seADevolucaoFalharOPedidoNaoESalvo() {
        doThrow(new RecursoNaoEncontradoException("Produto não encontrado"))
            .when(produtoService).reporEstoque(1L, 1);

        assertThrows(RecursoNaoEncontradoException.class,
            () -> lifecycleService.atualizarStatus(pedidoBase, StatusPedido.CANCELADO));

        verify(pedidoRepository, never()).save(any());
    }

    // Leva o pedido até o status desejado usando só transições válidas,
    // já que alterarStatus não permite pular etapas.
    private void levarPedidoAte(StatusPedido alvo) {
        List<StatusPedido> caminho = switch (alvo) {
            case CRIADO -> List.of();
            case EM_PREPARO -> List.of(StatusPedido.EM_PREPARO);
            case PRONTO -> List.of(StatusPedido.EM_PREPARO, StatusPedido.PRONTO);
            case ENTREGUE -> List.of(StatusPedido.EM_PREPARO, StatusPedido.PRONTO, StatusPedido.ENTREGUE);
            case CANCELADO -> List.of(StatusPedido.CANCELADO);
        };
        caminho.forEach(pedidoBase::alterarStatus);
    }
}