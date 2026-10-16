package com.lanchonete.gerenciadorlanchonetebackend.compras.repository;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
