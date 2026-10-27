package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_pedido", joinColumns = @JoinColumn(name = "pedido_id"))
    private List<ItemPedido> itens;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    protected Pedido() {
    }

    public Pedido(Long clienteId, List<ItemPedido> itens) {
        validarClienteId(clienteId);
        validarItens(itens);

        this.clienteId = clienteId;
        this.itens = List.copyOf(itens);
        this.total = calcularTotalDosItens(this.itens);
        this.criadoEm = LocalDateTime.now();
        this.status = StatusPedido.CRIADO;
    }

    public static Pedido criarAPartirDoCarrinho(Long clienteId, CarrinhoCompras carrinho) {
        if (carrinho == null) {
            throw new IllegalArgumentException("O carrinho não pode ser nulo.");
        }

        if (carrinho.estaVazio()) {
            throw new IllegalArgumentException("Não é possível criar um pedido com carrinho vazio.");
        }

        List<ItemPedido> itensPedido = carrinho.listarItens().stream()
            .map(ItemPedido::criarAPartirDoItemCarrinho)
            .toList();

        return new Pedido(clienteId, itensPedido);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void alterarStatus(StatusPedido novo) {
        if (novo == null) throw new IllegalArgumentException("O novo status não pode ser nulo.");
        if (!status.podeTransitarPara(novo)) throw new TransicaoStatusInvalidaException(status, novo);
        this.status = novo;
    }

    private BigDecimal calcularTotalDosItens(List<ItemPedido> itens) {
        return itens.stream()
            .map(ItemPedido::calcularSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarClienteId(Long clienteId) {
        if (clienteId == null || clienteId <= 0) {
            throw new IllegalArgumentException("O identificador do cliente deve ser positivo.");
        }
    }

    private void validarItens(List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve possuir pelo menos um item.");
        }
    }
}
