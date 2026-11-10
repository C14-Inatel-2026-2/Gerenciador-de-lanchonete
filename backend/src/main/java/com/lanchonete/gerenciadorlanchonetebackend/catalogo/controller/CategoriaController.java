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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        validarAdministrador("Apenas administradores podem criar categorias");
        validarRequest(request);

        Categoria categoria = categoriaService.salvar(new Categoria(request.nome()));
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResposta(categoria));
    }

    @PutMapping("/{id}")
    public CategoriaResponse editarCategoria(
            @PathVariable Long id,
            @RequestBody CategoriaRequest request
    ) {
        validarAdministrador("Apenas administradores podem gerenciar categorias");
        validarRequest(request);
        return paraResposta(categoriaService.editar(id, request.nome()));
    }

    @PatchMapping("/{id}/desativar")
    public CategoriaResponse desativarCategoria(@PathVariable Long id) {
        validarAdministrador("Apenas administradores podem gerenciar categorias");
        return paraResposta(categoriaService.desativar(id));
    }

    private void validarAdministrador(String mensagem) {
        var usuario = usuarioAtualProvider.obterUsuarioAtual();
        if (!authorizationService.podeGerenciarCatalogo(usuario)) {
            throw new AcessoNegadoException(mensagem);
        }
    }

    private void validarRequest(CategoriaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição é obrigatório");
        }
    }

    private CategoriaResponse paraResposta(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.isAtivo());
    }
}
