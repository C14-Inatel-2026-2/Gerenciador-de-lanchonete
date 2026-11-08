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

    public Categoria editar(Long id, String nome) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
        categoria.setNome(nome);
        validarNome(categoria);
        if (categoriaRepository.existsByNomeAndIdNot(nome, id)) {
            throw new IllegalArgumentException("Já existe uma categoria cadastrada com este nome");
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria desativar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
        categoria.setAtivo(false);
        return categoriaRepository.save(categoria);
    }

    private void validar(Categoria categoria) {
        validarNome(categoria);
        if(categoriaRepository.existsByNome(categoria.getNome())){
            throw new IllegalArgumentException("Já existe uma categoria cadastrada com este nome");
        }
    }

    private void validarNome(Categoria categoria) {
        if(categoria.getNome() == null || categoria.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
    }

    public List<Categoria> listarCategorias(){
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarCategoriasAtivas() {
        return categoriaRepository.findByAtivoTrue();
    }
}
