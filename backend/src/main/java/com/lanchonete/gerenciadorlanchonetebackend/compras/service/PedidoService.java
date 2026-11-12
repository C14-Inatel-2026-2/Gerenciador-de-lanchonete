package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.ItemPedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.PedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.CarrinhoCompras;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemCarrinho;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.repository.PedidoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoService produtoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            ProdutoService produtoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.produtoService = produtoService;
    }

    @Transactional
    public Pedido criarPedidoComItens(Long clienteId, PedidoRequest request) {
        validarClienteId(clienteId);
        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve possuir pelo menos um item.");
        }

        Map<Long, Integer> quantidadesPorProduto = new LinkedHashMap<>();
        for (ItemPedidoRequest item : request.itens()) {
            if (item == null || item.produtoId() == null || item.produtoId() <= 0) {
                throw new IllegalArgumentException("O identificador do produto deve ser positivo.");
            }
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
            }

            int quantidadeAtual = quantidadesPorProduto.getOrDefault(item.produtoId(), 0);
            if (quantidadeAtual > Integer.MAX_VALUE - item.quantidade()) {
                throw new IllegalArgumentException("A quantidade total do produto excede o limite permitido.");
            }
            quantidadesPorProduto.put(item.produtoId(), quantidadeAtual + item.quantidade());
        }

        CarrinhoCompras carrinho = new CarrinhoCompras();
        for (Map.Entry<Long, Integer> item : quantidadesPorProduto.entrySet()) {
            Produto produto = produtoRepository.findById(item.getKey())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
            ItemCarrinho itemCarrinho = new ItemCarrinho(produto, item.getValue());
            produtoService.baixarEstoque(produto, item.getValue());
            carrinho.adicionarItem(itemCarrinho);
        }

        return criarPedido(clienteId, carrinho);
    }

    public Pedido criarPedido(Long clienteId, CarrinhoCompras carrinho) {
        validarClienteId(clienteId);

        if (carrinho == null || carrinho.estaVazio()) {
            throw new IllegalArgumentException("Não é possível criar um pedido com carrinho vazio.");
        }

        Pedido pedido = carrinho.gerarPedido(clienteId);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        carrinho.limparCarrinho();
        return pedidoSalvo;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosDoCliente(Long clienteId) {
        validarClienteId(clienteId);
        return pedidoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId);
    }

    private void validarClienteId(Long clienteId) {
        if (clienteId == null || clienteId <= 0) {
            throw new IllegalArgumentException("O identificador do cliente deve ser positivo.");
        }
    }
}
