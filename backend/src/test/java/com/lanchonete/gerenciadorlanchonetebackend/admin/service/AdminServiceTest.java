package com.lanchonete.gerenciadorlanchonetebackend.admin.service;

import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.PedidoRecenteResponse;
import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.ProdutoEstoqueBaixoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.ResumoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.compras.ProdutoTestFactory;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.config.FusoHorarioConfig;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    // 12/11/2026 às 22h30 em Brasília = 13/11 às 01h30 em UTC
    private static final Clock RELOGIO = Clock.fixed(
            Instant.parse("2026-11-13T01:30:00Z"), FusoHorarioConfig.FUSO_DA_LANCHONETE);

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(pedidoRepository, produtoRepository, userProfileRepository, RELOGIO);
    }

    private void pedidosDeHoje(Pedido... pedidos) {
        when(pedidoRepository.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(any(), any()))
                .thenReturn(List.of(pedidos));
    }

    @Test
    void hojeEODiaDeBrasiliaMesmoQuandoEmUtcJaEAmanha() {
        pedidosDeHoje();

        service.obterResumo();

        verify(pedidoRepository).findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(
                LocalDateTime.of(2026, 11, 12, 0, 0),
                LocalDateTime.of(2026, 11, 13, 0, 0));
    }

    @Test
    void canceladoNaoEntraNoFaturamentoNemNoTicketMedio() {
        pedidosDeHoje(
                pedido(1L, 10L, "20.00", StatusPedido.CRIADO),
                pedido(2L, 10L, "30.00", StatusPedido.EM_PREPARO),
                pedido(3L, 10L, "100.00", StatusPedido.CANCELADO));

        ResumoResponse resumo = service.obterResumo();

        assertEquals(3, resumo.pedidosHoje()); // o cancelado continua sendo um pedido do dia
        assertEquals(new BigDecimal("50.00"), resumo.faturamentoHoje());
        assertEquals(new BigDecimal("25.00"), resumo.ticketMedio());
    }

    @Test
    void semPedidosOTicketMedioEZeroSemDividirPorZero() {
        pedidosDeHoje();

        ResumoResponse resumo = service.obterResumo();

        assertEquals(0, resumo.pedidosHoje());
        assertEquals(new BigDecimal("0.00"), resumo.faturamentoHoje());
        assertEquals(new BigDecimal("0.00"), resumo.ticketMedio());
    }

    @Test
    void soCanceladosNoDiaTambemDaTicketMedioZero() {
        pedidosDeHoje(pedido(1L, 10L, "40.00", StatusPedido.CANCELADO));

        ResumoResponse resumo = service.obterResumo();

        assertEquals(new BigDecimal("0.00"), resumo.faturamentoHoje());
        assertEquals(new BigDecimal("0.00"), resumo.ticketMedio());
    }

    @Test
    void ticketMedioArredondaParaDuasCasas() {
        pedidosDeHoje(
                pedido(1L, 10L, "10.00", StatusPedido.CRIADO),
                pedido(2L, 10L, "10.00", StatusPedido.CRIADO),
                pedido(3L, 10L, "12.50", StatusPedido.CRIADO));

        assertEquals(new BigDecimal("10.83"), service.obterResumo().ticketMedio());
    }

    @Test
    void pedidosPorStatusTrazTodosOsStatusMesmoComZero() {
        pedidosDeHoje(
                pedido(1L, 10L, "10.00", StatusPedido.CRIADO),
                pedido(2L, 10L, "10.00", StatusPedido.CRIADO),
                pedido(3L, 10L, "10.00", StatusPedido.CANCELADO));

        var porStatus = service.obterResumo().pedidosPorStatus();

        assertEquals(StatusPedido.values().length, porStatus.size());
        assertEquals(2L, porStatus.get(StatusPedido.CRIADO));
        assertEquals(1L, porStatus.get(StatusPedido.CANCELADO));
        assertEquals(0L, porStatus.get(StatusPedido.ENTREGUE));
    }

    @Test
    void contaProdutosAtivosEUsuariosCadastrados() {
        pedidosDeHoje();
        when(produtoRepository.countByDisponivelTrue()).thenReturn(32L);
        when(userProfileRepository.count()).thenReturn(18L);

        ResumoResponse resumo = service.obterResumo();

        assertEquals(32, resumo.produtosAtivos());
        assertEquals(18, resumo.usuariosCadastrados());
    }

    @Test
    void pedidosRecentesTrazemONomeDoCliente() {
        pedidosDeHoje();
        when(pedidoRepository.findTop5ByOrderByCriadoEmDesc()).thenReturn(List.of(
                pedido(8L, 10L, "25.00", StatusPedido.PRONTO),
                pedido(7L, 99L, "12.00", StatusPedido.CRIADO)));
        when(userProfileRepository.findByIdIn(any())).thenReturn(List.of(cliente(10L, "Maria")));

        List<PedidoRecenteResponse> recentes = service.obterResumo().pedidosRecentes();

        assertEquals(2, recentes.size());
        assertEquals(8L, recentes.get(0).id());
        assertEquals("Maria", recentes.get(0).cliente());
        assertEquals(StatusPedido.PRONTO, recentes.get(0).status());
        assertEquals(new BigDecimal("25.00"), recentes.get(0).total());
        // Cliente que não existe mais no banco: o pedido aparece mesmo assim
        assertEquals("Cliente #99", recentes.get(1).cliente());
    }

    @Test
    void semPedidosRecentesNaoConsultaOsClientes() {
        pedidosDeHoje();
        when(pedidoRepository.findTop5ByOrderByCriadoEmDesc()).thenReturn(List.of());

        assertTrue(service.obterResumo().pedidosRecentes().isEmpty());
        verify(userProfileRepository, never()).findByIdIn(any());
    }

    // ---- estoque baixo (#56)

    @Test
    void produtoComEstoque3ApareceNoAlertaEComEstoque10Nao() {
        pedidosDeHoje();
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(
                produto(1L, "X-Burguer", 3),
                produto(2L, "Batata frita", 10)));

        List<ProdutoEstoqueBaixoResponse> alerta = service.obterResumo().produtosComEstoqueBaixo();

        assertEquals(List.of(new ProdutoEstoqueBaixoResponse(1L, "X-Burguer", 3)), alerta);
    }

    @Test
    void limiteDoAlertaE5Unidades() {
        pedidosDeHoje();
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(
                produto(1L, "Com cinco", 5),
                produto(2L, "Com seis", 6)));

        List<ProdutoEstoqueBaixoResponse> alerta = service.obterResumo().produtosComEstoqueBaixo();

        assertEquals(1, alerta.size());
        assertEquals("Com cinco", alerta.get(0).nome());
    }

    @Test
    void esgotadosVemPrimeiroEDepoisPorNome() {
        pedidosDeHoje();
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(
                produto(1L, "Suco", 4),
                produto(2L, "Refrigerante", 0),
                produto(3L, "Açaí", 4)));

        List<String> nomes = service.obterResumo().produtosComEstoqueBaixo().stream()
                .map(ProdutoEstoqueBaixoResponse::nome)
                .toList();

        assertEquals(List.of("Refrigerante", "Açaí", "Suco"), nomes);
    }

    @Test
    void semProdutosComEstoqueBaixoAListaVemVazia() {
        pedidosDeHoje();
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(produto(1L, "X-Burguer", 20)));

        assertTrue(service.obterResumo().produtosComEstoqueBaixo().isEmpty());
    }

    private Produto produto(Long id, String nome, int estoque) {
        Produto produto = ProdutoTestFactory.produto(id, nome, "10.00");
        produto.setQuantidadeEstoque(estoque);
        return produto;
    }

    private Pedido pedido(Long id, Long clienteId, String total, StatusPedido status) {
        Pedido pedido = new Pedido(clienteId, List.of(new ItemPedido(1L, "Lanche", new BigDecimal(total), 1)));
        pedido.setId(id);
        if (status == StatusPedido.PRONTO) {
            pedido.alterarStatus(StatusPedido.EM_PREPARO);
        }
        if (status != StatusPedido.CRIADO) {
            pedido.alterarStatus(status);
        }
        return pedido;
    }

    private UserProfile cliente(Long id, String nome) {
        UserProfile perfil = new UserProfile("sub-" + id, nome, nome.toLowerCase() + "@email.com", UserRole.CLIENTE);
        ReflectionTestUtils.setField(perfil, "id", id);
        return perfil;
    }
}
