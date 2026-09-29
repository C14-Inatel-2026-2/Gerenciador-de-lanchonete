package com.lanchonete.gerenciadorlanchonetebackend.compras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    // 1. TESTE SEM MOCK (Regra Positiva)
    @Test
    void deveAvancarStatusDeCriadoParaEmPreparoSemMock() {
        lifecycleService.avancarParaPreparo(pedidoBase);
        assertEquals(StatusPedido.EM_PREPARO, pedidoBase.getStatus());
    }

    // 2. TESTE SEM MOCK (Regra Negativa / Exceção)
    @Test
    void deveLancarExcecaoAoTentarCancelarPedidoPronto() {
        pedidoBase.setStatus(StatusPedido.PRONTO);

        IllegalStateException excecao = assertThrows(IllegalStateException.class, () -> {
            lifecycleService.cancelarPedido(pedidoBase);
        });

        assertEquals("Não é possível cancelar um pedido que já está PRONTO ou ENTREGUE.", excecao.getMessage());
    }

    // 3. TESTE COM MOCK (Salvar Status)
    @Test
    void deveAtualizarStatusESalvarNoBancoComMock() {
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoBase);

        Pedido atualizado = lifecycleService.atualizarStatusNoBanco(pedidoBase, StatusPedido.PRONTO);

        assertEquals(StatusPedido.PRONTO, atualizado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }

    // 4. TESTE COM MOCK (Cancelar e Salvar)
    @Test
    void deveCancelarPedidoValidoESalvarNoBancoComMock() {
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoBase);

        Pedido cancelado = lifecycleService.cancelarPedido(pedidoBase);

        assertEquals(StatusPedido.CANCELADO, cancelado.getStatus());
        verify(pedidoRepository, times(1)).save(pedidoBase);
    }
}