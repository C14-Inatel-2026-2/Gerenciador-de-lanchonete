package com.lanchonete.gerenciadorlanchonetebackend.compras;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Pedido {

    private Long id;
    private final Long clienteId;
    private final List<ItemPedido> itens;
    private final BigDecimal total;
    private final LocalDateTime criadoEm;
    private StatusPedido status;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setStatus(StatusPedido status) { this.status = status; }
}
