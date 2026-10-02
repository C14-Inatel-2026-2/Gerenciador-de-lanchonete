package com.lanchonete.gerenciadorlanchonetebackend.compras;

import java.math.BigDecimal;
import java.util.Objects;

public class ItemCarrinho {
    private final Long produtoId;
    private final String nome;
    private final BigDecimal precoUnitario;
    private int quantidade;

    public ItemCarrinho(Long produtoId, String nome, BigDecimal precoUnitario, int quantidade) {
        validarProdutoId(produtoId);
        validarNome(nome);
        validarPrecoUnitario(precoUnitario);
        validarQuantidade(quantidade);

        this.produtoId = produtoId;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
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
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof ItemCarrinho outroItem)) {
            return false;
        }

        return produtoId.equals(outroItem.produtoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId);
    }

    private void validarProdutoId(Long produtoId) {
        if (produtoId == null || produtoId <= 0) {
            throw new IllegalArgumentException("O identificador do produto deve ser positivo.");
        }
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

    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }
}
