package com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CategoriaRequest;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CategoriaResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.CategoriaService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService categoriaService;
    private final UsuarioAtualProvider usuarioAtualProvider;
    private final AuthorizationService authorizationService;

    public CategoriaController(
            CategoriaService categoriaService,
            UsuarioAtualProvider usuarioAtualProvider,
            AuthorizationService authorizationService
    ) {
        this.categoriaService = categoriaService;
        this.usuarioAtualProvider = usuarioAtualProvider;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<CategoriaResponse> listarCategorias() {
        return categoriaService.listarCategoriasAtivas().stream()
                .map(this::paraResposta)
                .toList();
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criarCategoria(@RequestBody CategoriaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição é obrigatório");
        }

        var usuario = usuarioAtualProvider.obterUsuarioAtual();
        if (!authorizationService.podeGerenciarCatalogo(usuario)) {
            throw new AcessoNegadoException("Apenas administradores podem criar categorias");
        }

        Categoria categoria = categoriaService.salvar(new Categoria(request.nome()));
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResposta(categoria));
    }

    private CategoriaResponse paraResposta(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.isAtivo());
    }
}
