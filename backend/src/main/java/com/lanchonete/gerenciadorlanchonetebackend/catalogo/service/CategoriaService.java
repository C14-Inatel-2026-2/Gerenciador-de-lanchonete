package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria salvar(Categoria categoria) {
        validar(categoria);
        return categoriaRepository.save(categoria);
    }

    private void validar(Categoria categoria) {
        if(categoria.getNome() == null || categoria.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        if(categoriaRepository.existsByNome(categoria.getNome())){
            throw new IllegalArgumentException("Já existe uma categoria cadastrada com este nome");
        }
    }

    public List<Categoria> listarCategorias(){
        return categoriaRepository.findAll();
    }
}
