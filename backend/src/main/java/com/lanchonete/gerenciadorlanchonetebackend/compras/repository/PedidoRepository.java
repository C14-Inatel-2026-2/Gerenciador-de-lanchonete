package com.lanchonete.gerenciadorlanchonetebackend.compras.repository;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Query derivada do nome do método: o Spring Data gera
    // WHERE status IN (...) ORDER BY criado_em ASC (RF-CIC05).
    List<Pedido> findByStatusInOrderByCriadoEmAsc(Collection<StatusPedido> status);
}