package com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
