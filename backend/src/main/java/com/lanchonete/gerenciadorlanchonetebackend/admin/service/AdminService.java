package com.lanchonete.gerenciadorlanchonetebackend.admin.service;

import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.PedidoRecenteResponse;
import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.ResumoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Números do painel administrativo (US05 · #48). Quem pode ver o painel é
 * decidido no AdminController; aqui só se calcula o resumo.
 */
@Service
public class AdminService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UserProfileRepository userProfileRepository;
    private final Clock relogio;

    public AdminService(
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            UserProfileRepository userProfileRepository,
            Clock relogio
    ) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.userProfileRepository = userProfileRepository;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public ResumoResponse obterResumo() {
        List<Pedido> pedidosDeHoje = buscarPedidosDeHoje();
        // Cancelado não é venda: fica fora do faturamento e do ticket médio
        List<Pedido> vendas = pedidosDeHoje.stream()
                .filter(pedido -> pedido.getStatus() != StatusPedido.CANCELADO)
                .toList();
        BigDecimal faturamento = somarTotais(vendas);

        return new ResumoResponse(
                pedidosDeHoje.size(),
                faturamento,
                calcularTicketMedio(faturamento, vendas.size()),
                contarPorStatus(pedidosDeHoje),
                produtoRepository.countByDisponivelTrue(),
                userProfileRepository.count(),
                listarPedidosRecentes()
        );
    }

    /** Do início de hoje (00:00) até o início de amanhã, sem incluir, no fuso da lanchonete. */
    private List<Pedido> buscarPedidosDeHoje() {
        LocalDate hoje = LocalDate.now(relogio);
        return pedidoRepository.findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(
                hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
    }

    private BigDecimal somarTotais(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Sem vendas no dia, o ticket médio é zero (não divide por zero). */
    private BigDecimal calcularTicketMedio(BigDecimal faturamento, int quantidadeDeVendas) {
        if (quantidadeDeVendas == 0) {
            return ZERO;
        }
        return faturamento.divide(BigDecimal.valueOf(quantidadeDeVendas), 2, RoundingMode.HALF_UP);
    }

    /** Todos os status aparecem, mesmo com zero, para o painel não precisar tratar ausência. */
    private Map<StatusPedido, Long> contarPorStatus(List<Pedido> pedidos) {
        Map<StatusPedido, Long> contagem = new EnumMap<>(StatusPedido.class);
        for (StatusPedido status : StatusPedido.values()) {
            contagem.put(status, 0L);
        }
        pedidos.forEach(pedido -> contagem.merge(pedido.getStatus(), 1L, Long::sum));
        return contagem;
    }

    private List<PedidoRecenteResponse> listarPedidosRecentes() {
        List<Pedido> recentes = pedidoRepository.findTop5ByOrderByCriadoEmDesc();
        if (recentes.isEmpty()) {
            return List.of();
        }

        // Uma consulta só para os nomes, em vez de uma por pedido
        Map<Long, String> nomes = userProfileRepository
                .findByIdIn(recentes.stream().map(Pedido::getClienteId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(UserProfile::getId, UserProfile::getNome));

        return recentes.stream()
                .map(pedido -> new PedidoRecenteResponse(
                        pedido.getId(),
                        // Cliente apagado do banco: o pedido continua aparecendo
                        nomes.getOrDefault(pedido.getClienteId(), "Cliente #" + pedido.getClienteId()),
                        pedido.getStatus(),
                        pedido.getTotal(),
                        pedido.getCriadoEm()))
                .toList();
    }
}
