package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.TransicaoStatusInvalidaException;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoLifecycleServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

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

        Pedido atualizado = lifecycleService.avancarParaPreparo(pedidoBase);

        assertEquals(StatusPedido.EM_PREPARO, atualizado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void deveCancelarPedidoCriadoESalvarNoBanco() {
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = lifecycleService.cancelarPedido(pedidoBase);

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
            () -> lifecycleService.cancelarPedido(pedidoBase));

        assertEquals(statusAtual, pedidoBase.getStatus());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void naoDeveAceitarPedidoNulo() {
        assertThrows(IllegalArgumentException.class,
            () -> lifecycleService.avancarParaPreparo(null));
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

        Pedido atualizado = lifecycleService.marcarComoPronto(pedidoBase);

        assertEquals(StatusPedido.PRONTO, atualizado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void deveEntregarPedidoPronto() {
        levarPedidoAte(StatusPedido.PRONTO);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido atualizado = lifecycleService.entregar(pedidoBase);

        assertEquals(StatusPedido.ENTREGUE, atualizado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    @Test
    void deveCancelarPedidoEmPreparo() {
        levarPedidoAte(StatusPedido.EM_PREPARO);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = lifecycleService.cancelarPedido(pedidoBase);

        assertEquals(StatusPedido.CANCELADO, cancelado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
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

