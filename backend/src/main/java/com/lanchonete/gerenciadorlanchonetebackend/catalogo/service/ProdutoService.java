package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CategoriaCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.ProdutoCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
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
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Produto salvar(Produto produto) {
        validarDados(produto.getNome(), produto.getPreco(), produto.getCategoria());
        produto.setCategoria(obterCategoriaAtiva(produto.getCategoria().getId()));
        if (produtoRepository.existsByNome(produto.getNome())) {
            throw new IllegalArgumentException("Já existe um produto cadastrado com este nome");
        }
        return produtoRepository.save(produto);
    }

    public Produto editar(Long id, String nome, String descricao, BigDecimal preco, Long categoriaId) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        validarDados(nome, preco, categoriaId);
        Categoria categoria = obterCategoriaAtiva(categoriaId);
        if (produtoRepository.existsByNomeAndIdNot(nome, id)) {
            throw new IllegalArgumentException("Já existe um produto cadastrado com este nome");
        }

        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setCategoria(categoria);
        return produtoRepository.save(produto);
    }

    private void validarDados(String nome, BigDecimal preco, Categoria categoria) {
        validarDados(nome, preco, categoria == null ? null : categoria.getId());
    }

    private void validarDados(String nome, BigDecimal preco, Long categoriaId) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
        if(preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
        }
        if(categoriaId == null) {
            throw new IllegalArgumentException("Produto deve ter uma categoria");
        }
    }

    private Categoria obterCategoriaAtiva(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
        if (!categoria.isAtivo()) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada");
        }
        return categoria;
    }

    public List<Produto> listarDisponiveis(){
        return produtoRepository.findByDisponivelTrue();
    }

    public List<Produto> listarProdutosDisponiveis(Long categoriaId) {
        if (categoriaId == null) {
            return produtoRepository.findByDisponivelTrue();
        }
        return produtoRepository.findByCategoriaIdAndDisponivelTrue(categoriaId);
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        produto.setDisponivel(false);
        return produtoRepository.save(produto);
    }
}
