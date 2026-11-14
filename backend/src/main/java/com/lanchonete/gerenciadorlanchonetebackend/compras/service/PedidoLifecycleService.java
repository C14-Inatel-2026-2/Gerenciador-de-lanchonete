package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoLifecycleService {

    //  pedidos que ainda precisam da cozinha (RF-CIC05).
    // ENTREGUE e CANCELADO saem da fila.
    static final List<StatusPedido> STATUS_DA_FILA =
        List.of(StatusPedido.CRIADO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO);

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;

    public PedidoLifecycleService(PedidoRepository pedidoRepository, ProdutoService produtoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
    }

    // fila da cozinha, do pedido mais antigo para o mais novo.
    public List<Pedido> listarFila() {
        return pedidoRepository.findByStatusInOrderByCriadoEmAsc(STATUS_DA_FILA);
    }

    // usado pela API. Busca o pedido pelo id (404 se não existir)
    // e reaproveita o mesmo fluxo de validação e persistência do método abaixo.
    // O @Transactional fica aqui também porque a chamada a atualizarStatus é interna
    // (mesma classe) e não passa pelo proxy do Spring.
    @Transactional
    public Pedido atualizarStatusPorId(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
        return atualizarStatus(pedido, novoStatus);
    }

    // Único ponto que altera status e persiste. A validação da transição
    // fica no próprio Pedido (alterarStatus), para não duplicar a regra.
    // Ao cancelar, devolve ao estoque a quantidade de cada item (#56), na mesma
    // transação da mudança de status: se a devolução falhar, o status não muda.
    @Transactional
    public Pedido atualizarStatus(Pedido pedido, StatusPedido novoStatus) {
        if (pedido == null) {
            throw new IllegalArgumentException("O pedido não pode ser nulo.");
        }
        pedido.alterarStatus(novoStatus);
        if (novoStatus == StatusPedido.CANCELADO) {
            devolverEstoque(pedido);
        }
        return pedidoRepository.save(pedido);
    }

    private void devolverEstoque(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            produtoService.reporEstoque(item.getProdutoId(), item.getQuantidade());
        }
    }
}