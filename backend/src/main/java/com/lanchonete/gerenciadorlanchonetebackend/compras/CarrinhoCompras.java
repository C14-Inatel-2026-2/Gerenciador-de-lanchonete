package com.lanchonete.gerenciadorlanchonetebackend.compras;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarrinhoCompras {
    private final List<ItemCarrinho> itens = new ArrayList<>();

    public void adicionarItem(ItemCarrinho item) {
        validarItem(item);

        buscarItemPorProdutoId(item.getProdutoId())
            .ifPresentOrElse(
                itemExistente -> itemExistente.aumentarQuantidade(item.getQuantidade()),
                () -> itens.add(item)
            );
    }

    public boolean removerItem(ItemCarrinho item) {
        validarItem(item);
        return itens.remove(item);
    }

    public boolean removerItemPorProdutoId(Long produtoId) {
        validarProdutoId(produtoId);
        return itens.removeIf(item -> item.getProdutoId().equals(produtoId));
    }

    public void alterarQuantidade(Long produtoId, int novaQuantidade) {
        validarProdutoId(produtoId);

        if (novaQuantidade <= 0) {
            removerItemPorProdutoId(produtoId);
            return;
        }

        ItemCarrinho item = buscarItemPorProdutoId(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("Item não encontrado no carrinho."));

        item.alterarQuantidade(novaQuantidade);
    }

    public List<ItemCarrinho> listarItens() {
        return List.copyOf(itens);
    }

    public int obterQuantidadeDeProdutosDiferentes() {
        return itens.size();
    }

    public int obterQuantidadeTotalDeItens() {
        return itens.stream()
            .mapToInt(ItemCarrinho::getQuantidade)
            .sum();
    }

    public int obterNumeroDeItens() {
        return obterQuantidadeDeProdutosDiferentes();
    }

    public boolean contemItem(ItemCarrinho item) {
        validarItem(item);
        return itens.contains(item);
    }

    public boolean contemProduto(Long produtoId) {
        return buscarItemPorProdutoId(produtoId).isPresent();
    }

    public Optional<ItemCarrinho> buscarItemPorProdutoId(Long produtoId) {
        validarProdutoId(produtoId);

        return itens.stream()
            .filter(item -> item.getProdutoId().equals(produtoId))
            .findFirst();
    }

    public BigDecimal calcularTotal() {
        return itens.stream()
            .map(ItemCarrinho::calcularSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Pedido gerarPedido(Long clienteId) {
        return Pedido.criarAPartirDoCarrinho(clienteId, this);
    }

    public boolean estaVazio() {
        return itens.isEmpty();
    }

    public void limparCarrinho() {
        itens.clear();
    }

    private void validarItem(ItemCarrinho item) {
        if (item == null) {
            throw new IllegalArgumentException("O item não pode ser nulo.");
        }
    }

    private void validarProdutoId(Long produtoId) {
        if (produtoId == null || produtoId <= 0) {
            throw new IllegalArgumentException("O identificador do produto deve ser positivo.");
        }
    }
}
