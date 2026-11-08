package com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<Produto> listarProdutos(@RequestParam(required = false) Long categoriaId) {
        return produtoService.listarProdutosDisponiveis(categoriaId);
    }

    @GetMapping("/cardapio")
    public CardapioResponse obterCardapio() {
        return produtoService.obterCardapio();
    }
}
