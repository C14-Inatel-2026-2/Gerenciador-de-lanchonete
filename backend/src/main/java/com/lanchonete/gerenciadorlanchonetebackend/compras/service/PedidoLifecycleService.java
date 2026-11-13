package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException; 
import org.springframework.stereotype.Service;

import java.util.List; 

@Service
public class PedidoLifecycleService {

    //  pedidos que ainda precisam da cozinha (RF-CIC05).
    // ENTREGUE e CANCELADO saem da fila.
    static final List<StatusPedido> STATUS_DA_FILA =
        List.of(StatusPedido.CRIADO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO);

    private final PedidoRepository pedidoRepository;

    public PedidoLifecycleService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // fila da cozinha, do pedido mais antigo para o mais novo.
    public List<Pedido> listarFila() {
        return pedidoRepository.findByStatusInOrderByCriadoEmAsc(STATUS_DA_FILA);
    }

    // usado pela API. Busca o pedido pelo id (404 se não existir)
    // e reaproveita o mesmo fluxo de validação e persistência do método abaixo.
    public Pedido atualizarStatusPorId(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
        return atualizarStatus(pedido, novoStatus);
    }

    // Único ponto que altera status e persiste. A validação da transição
    // fica no próprio Pedido (alterarStatus), para não duplicar a regra.
    public Pedido atualizarStatus(Pedido pedido, StatusPedido novoStatus) {
        if (pedido == null) {
            throw new IllegalArgumentException("O pedido não pode ser nulo.");
        }
        pedido.alterarStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }
}