package com.lanchonete.gerenciadorlanchonetebackend.compras.repository;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Painel administrativo (US05): pedidos de um dia, no intervalo [inicio, fim)
    List<Pedido> findByCriadoEmGreaterThanEqualAndCriadoEmLessThan(LocalDateTime inicio, LocalDateTime fim);

    // Painel administrativo (US05): os 5 pedidos mais recentes, de qualquer dia
    List<Pedido> findTop5ByOrderByCriadoEmDesc();

    // Query derivada do nome do método: o Spring Data gera
    // WHERE status IN (...) ORDER BY criado_em ASC (RF-CIC05).
    List<Pedido> findByStatusInOrderByCriadoEmAsc(Collection<StatusPedido> status);
}