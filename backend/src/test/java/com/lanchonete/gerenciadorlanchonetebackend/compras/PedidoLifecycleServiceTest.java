package com.lanchonete.gerenciadorlanchonetebackend.compras;

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
        pedidoBase.setStatus(statusAtual);

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
}
