package com.lanchonete.gerenciadorlanchonetebackend.catalogo.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "produto")

public class Produto {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean disponivel = true;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int quantidadeEstoque;

    @Version
    @ColumnDefault("0")
    private Long version;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    public Produto(){

    }

    public Produto(String nome, String descricao, BigDecimal preco, Categoria categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        if (quantidadeEstoque < 0) {
            throw new IllegalArgumentException("Estoque do produto não pode ser negativo");
        }
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public boolean isEsgotado() {
        return quantidadeEstoque == 0;
    }

    public void baixarEstoque(int quantidade) {
        validarQuantidadePositiva(quantidade);
        if (quantidade > quantidadeEstoque) {
            throw new EstoqueInsuficienteException(
                    "Estoque insuficiente: disponível " + quantidadeEstoque + ", solicitado " + quantidade
            );
        }
        quantidadeEstoque -= quantidade;
    }

    public void reporEstoque(int quantidade) {
        validarQuantidadePositiva(quantidade);
        if (quantidade > Integer.MAX_VALUE - quantidadeEstoque) {
            throw new IllegalArgumentException("Quantidade de estoque excede o limite permitido");
        }
        quantidadeEstoque += quantidade;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    private static void validarQuantidadePositiva(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }
}
