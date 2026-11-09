package com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.ProdutoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.ProdutoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;
    private final UsuarioAtualProvider usuarioAtualProvider;
    private final AuthorizationService authorizationService;

    public ProdutoController(
            ProdutoService produtoService,
            UsuarioAtualProvider usuarioAtualProvider,
            AuthorizationService authorizationService
    ) {
        this.produtoService = produtoService;
        this.usuarioAtualProvider = usuarioAtualProvider;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Produto> listarProdutos(@RequestParam(required = false) Long categoriaId) {
        return produtoService.listarProdutosDisponiveis(categoriaId);
    }

    @GetMapping("/cardapio")
    public CardapioResponse obterCardapio() {
        return produtoService.obterCardapio();
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criarProduto(@RequestBody ProdutoRequest request) {
        validarAdministrador();
        validarRequest(request);
        Categoria categoria = new Categoria();
        categoria.setId(request.categoriaId());
        Produto produto = new Produto(request.nome(), request.descricao(), request.preco(), categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResposta(produtoService.salvar(produto)));
    }

    @PutMapping("/{id}")
    public ProdutoResponse editarProduto(@PathVariable Long id, @RequestBody ProdutoRequest request) {
        validarAdministrador();
        validarRequest(request);
        Produto produto = produtoService.editar(
                id,
                request.nome(),
                request.descricao(),
                request.preco(),
                request.categoriaId()
        );
        return paraResposta(produto);
    }

    @PatchMapping("/{id}/desativar")
    public ProdutoResponse desativarProduto(@PathVariable Long id) {
        validarAdministrador();
        return paraResposta(produtoService.desativar(id));
    }

    private void validarAdministrador() {
        var usuario = usuarioAtualProvider.obterUsuarioAtual();
        if (!authorizationService.podeGerenciarCatalogo(usuario)) {
            throw new AcessoNegadoException("Apenas administradores podem gerenciar produtos");
        }
    }

    private void validarRequest(ProdutoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição é obrigatório");
        }
    }

    private ProdutoResponse paraResposta(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.isDisponivel(),
                produto.getCategoria().getId()
        );
    }
}
