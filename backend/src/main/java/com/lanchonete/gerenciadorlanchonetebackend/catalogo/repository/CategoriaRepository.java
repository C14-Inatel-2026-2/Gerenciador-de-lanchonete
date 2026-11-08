package com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
    List<Categoria> findByAtivoTrue();
}
