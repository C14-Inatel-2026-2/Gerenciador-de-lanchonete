package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CategoriaCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.ProdutoCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProdutoService {
    private static final String MENSAGEM_CARDAPIO_VAZIO =
            "Cardápio vazio: não há produtos disponíveis.";

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
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

    public CardapioResponse obterCardapio() {
        List<Produto> produtosDisponiveis = listarDisponiveis();
        if (produtosDisponiveis.isEmpty()) {
            return new CardapioResponse(List.of(), MENSAGEM_CARDAPIO_VAZIO);
        }

        Map<Long, Categoria> categoriasPorId = new LinkedHashMap<>();
        Map<Long, List<ProdutoCardapioResponse>> produtosPorCategoria = new LinkedHashMap<>();
        for (Produto produto : produtosDisponiveis) {
            Categoria categoria = produto.getCategoria();
            Long categoriaId = categoria.getId();
            categoriasPorId.putIfAbsent(categoriaId, categoria);
            produtosPorCategoria
                    .computeIfAbsent(categoriaId, id -> new ArrayList<>())
                    .add(new ProdutoCardapioResponse(
                            produto.getNome(),
                            produto.getDescricao(),
                            produto.getPreco()
                    ));
        }

        List<CategoriaCardapioResponse> categorias = produtosPorCategoria.entrySet().stream()
                .map(entry -> new CategoriaCardapioResponse(
                        categoriasPorId.get(entry.getKey()).getNome(),
                        entry.getValue()
                ))
                .toList();

        return new CardapioResponse(categorias, null);
    }

    public Produto desativar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        produto.setDisponivel(false);
        return produtoRepository.save(produto);
    }
}
