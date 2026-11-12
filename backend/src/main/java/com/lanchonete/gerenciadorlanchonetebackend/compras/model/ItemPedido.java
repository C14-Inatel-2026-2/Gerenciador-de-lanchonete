package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class ItemPedido {
    private Long produtoId;
    private String nome;
    private BigDecimal precoUnitario;
    private int quantidade;

    // Exigido pelo JPA; não use diretamente
    protected ItemPedido() {
    }

    public ItemPedido(Long produtoId, String nome, BigDecimal precoUnitario, int quantidade) {
        ValidacoesCompras.validarProdutoId(produtoId);
        validarNome(nome);
        validarPrecoUnitario(precoUnitario);
        ValidacoesCompras.validarQuantidade(quantidade);

        this.produtoId = produtoId;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    public static ItemPedido criarAPartirDoItemCarrinho(ItemCarrinho itemCarrinho) {
        if (itemCarrinho == null) {
            throw new IllegalArgumentException("O item do carrinho não pode ser nulo.");
        }

        Produto produto = itemCarrinho.getProduto();
        return new ItemPedido(produto.getId(), produto.getNome(), produto.getPreco(), itemCarrinho.getQuantidade());
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal calcularSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto não pode ser vazio.");
        }
    }

    private void validarPrecoUnitario(BigDecimal precoUnitario) {
        if (precoUnitario == null || precoUnitario.signum() < 0) {
            throw new IllegalArgumentException("O preco unitario não pode ser negativo.");
        }
    }
}
