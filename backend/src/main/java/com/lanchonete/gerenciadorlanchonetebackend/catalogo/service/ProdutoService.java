package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository prodtoRepository) {
        this.produtoRepository = prodtoRepository;
    }

    public Produto salvar(Produto produto) {
        validar(produto);
        return produtoRepository.save(produto);
    }

    private void validar(Produto produto) {
        if(produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
        }
        if(produto.getCategoria() == null) {
            throw new IllegalArgumentException("Produto deve ter uma categoria");
        }
        if(produtoRepository.existsByNome(produto.getNome())){
            throw new IllegalArgumentException("Já existe um produto cadastrado com este nome");
        }
    }

    public List<Produto> listarDisponiveis(){
        return produtoRepository.findByDisponivelTrue();
    }

    public Produto desativar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        produto.setDisponivel(false);
        return produtoRepository.save(produto);
    }
}
