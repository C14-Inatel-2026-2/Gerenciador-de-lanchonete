package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CategoriaCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.ProdutoCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.ProdutoDuplicadoException;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        validarEstoque(produto.getQuantidadeEstoque());
        produto.setNome(produto.getNome().trim());
        produto.setCategoria(obterCategoriaAtiva(produto.getCategoria().getId()));
        if (produtoRepository.existsByNomeIgnoreCase(produto.getNome())) {
            throw new ProdutoDuplicadoException("Já existe um produto cadastrado com este nome");
        }
        return produtoRepository.save(produto);
    }

    public Produto editar(Long id, String nome, String descricao, BigDecimal preco, Long categoriaId) {
        return editar(id, nome, descricao, preco, categoriaId, null);
    }

    public Produto editar(
            Long id,
            String nome,
            String descricao,
            BigDecimal preco,
            Long categoriaId,
            Integer quantidadeEstoque
    ) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        validarDados(nome, preco, categoriaId);
        if (quantidadeEstoque != null) {
            validarEstoque(quantidadeEstoque);
        }
        String nomeNormalizado = nome.trim();
        Categoria categoria = obterCategoriaAtiva(categoriaId);
        if (produtoRepository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, id)) {
            throw new ProdutoDuplicadoException("Já existe um produto cadastrado com este nome");
        }

        produto.setNome(nomeNormalizado);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setCategoria(categoria);
        if (quantidadeEstoque != null) {
            produto.setQuantidadeEstoque(quantidadeEstoque);
        }
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto baixarEstoque(Long produtoId, int quantidade) {
        Produto produto = obterProduto(produtoId);
        produto.baixarEstoque(quantidade);
        return salvarComControleDeConcorrencia(produto);
    }

    @Transactional
    public Produto reporEstoque(Long produtoId, int quantidade) {
        Produto produto = obterProduto(produtoId);
        produto.reporEstoque(quantidade);
        return salvarComControleDeConcorrencia(produto);
    }

    @Transactional
    public Produto atualizarEstoque(Long produtoId, Integer quantidade) {
        if (quantidade == null) {
            throw new IllegalArgumentException("Quantidade de estoque é obrigatória");
        }
        validarEstoque(quantidade);
        Produto produto = obterProduto(produtoId);
        produto.setQuantidadeEstoque(quantidade);
        return produtoRepository.save(produto);
    }

    private void validarDados(String nome, BigDecimal preco, Categoria categoria) {
        validarDados(nome, preco, categoria == null ? null : categoria.getId());
    }

    private void validarDados(String nome, BigDecimal preco, Long categoriaId) {
        validarNome(nome);
        validarPreco(preco);
        validarCategoria(categoriaId);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
    }

    private void validarPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
        }
    }

    private void validarCategoria(Long categoriaId) {
        if (categoriaId == null) {
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
                            produto.getId(),
                            produto.getNome(),
                            produto.getDescricao(),
                            produto.getPreco(),
                            produto.getQuantidadeEstoque(),
                            produto.isEsgotado()
                    ));
        }

        List<CategoriaCardapioResponse> categorias = produtosPorCategoria.entrySet().stream()
                .map(entry -> new CategoriaCardapioResponse(
                        categoriasPorId.get(entry.getKey()).getId(),
                        categoriasPorId.get(entry.getKey()).getNome(),
                        entry.getValue()
                ))
                .toList();

        return new CardapioResponse(categorias, null);
    }

    public Produto desativar(Long id) {
        Produto produto = obterProduto(id);
        produto.setDisponivel(false);
        return produtoRepository.save(produto);
    }

    private Produto obterProduto(Long produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }

    private void validarEstoque(int quantidadeEstoque) {
        if (quantidadeEstoque < 0) {
            throw new IllegalArgumentException("Estoque do produto não pode ser negativo");
        }
    }

    private Produto salvarComControleDeConcorrencia(Produto produto) {
        try {
            return produtoRepository.saveAndFlush(produto);
        } catch (OptimisticLockingFailureException ex) {
            throw new EstoqueInsuficienteException(
                    "O estoque foi alterado por outra operação; atualize e tente novamente"
            );
        }
    }
}
