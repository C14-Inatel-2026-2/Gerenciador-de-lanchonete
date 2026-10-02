package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;

import java.math.BigDecimal;
import java.util.Objects;

public class ItemCarrinho {
    private final Produto produto;
    private int quantidade;

    public ItemCarrinho(Produto produto, int quantidade) {
        validarProduto(produto);
        validarQuantidade(quantidade);

        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public Long getProdutoId() {
        return produto.getId();
    }

    public String getNome() {
        return produto.getNome();
    }

    public BigDecimal getPrecoUnitario() {
        return produto.getPreco();
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void aumentarQuantidade(int quantidadeAdicional) {
        validarQuantidade(quantidadeAdicional);
        quantidade += quantidadeAdicional;
    }

    public void alterarQuantidade(int novaQuantidade) {
        validarQuantidade(novaQuantidade);
        quantidade = novaQuantidade;
    }

    public void diminuirQuantidade(int quantidadeRemovida) {
        validarQuantidade(quantidadeRemovida);

        if (quantidadeRemovida >= quantidade) {
            throw new IllegalArgumentException(
                "A quantidade removida deve ser menor que a quantidade atual do item."
            );
        }

        quantidade -= quantidadeRemovida;
    }

    public BigDecimal calcularSubtotal() {
        return produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof ItemCarrinho outroItem)) {
            return false;
        }

        return produto.getId().equals(outroItem.produto.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(produto.getId());
    }

    private void validarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo.");
        }

        if (produto.getId() == null || produto.getId() <= 0) {
            throw new IllegalArgumentException("O produto deve possuir um identificador válido.");
        }

        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do produto não pode ser vazio.");
        }

        if (produto.getPreco() == null || produto.getPreco().signum() < 0) {
            throw new IllegalArgumentException("O preco unitario não pode ser negativo.");
        }
    }

    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
