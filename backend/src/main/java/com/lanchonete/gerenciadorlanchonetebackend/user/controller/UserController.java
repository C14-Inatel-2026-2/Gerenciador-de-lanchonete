package com.lanchonete.gerenciadorlanchonetebackend.user.controller;

import com.lanchonete.gerenciadorlanchonetebackend.user.dto.AlterarRoleRequest;
import com.lanchonete.gerenciadorlanchonetebackend.user.dto.PerfilRequest;
import com.lanchonete.gerenciadorlanchonetebackend.user.dto.UsuarioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Perfil do usuário (US01, #42) e gerenciamento de usuários pelo ADMIN (US07, #43).
 * As regras de permissão ficam no UserService: o controller só identifica
 * quem está logado e converte entrada e saída (docs/api.md).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;
    private final UsuarioAtualProvider usuarioAtualProvider;

    public UserController(UserService userService, UsuarioAtualProvider usuarioAtualProvider) {
        this.userService = userService;
        this.usuarioAtualProvider = usuarioAtualProvider;
    }

    /**
     * Cria o perfil de quem acabou de se cadastrar no Supabase. Ainda não existe
     * perfil, então o id e o e-mail vêm direto do token, nunca do corpo.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@AuthenticationPrincipal Jwt token, @RequestBody PerfilRequest request) {
        return UsuarioResponse.de(
                userService.criarPerfil(token.getSubject(), request.nome(), token.getClaimAsString("email"))
        );
    }

    @GetMapping("/me")
    public UsuarioResponse meuPerfil() {
        return UsuarioResponse.de(usuarioAtualProvider.obterUsuarioAtual());
    }

    @PatchMapping("/me")
    public UsuarioResponse atualizarMeuPerfil(@RequestBody PerfilRequest request) {
        return UsuarioResponse.de(
                userService.atualizarNome(usuarioAtualProvider.obterUsuarioAtual(), request.nome())
        );
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return userService.listar(usuarioAtualProvider.obterUsuarioAtual()).stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @PatchMapping("/{id}/role")
    public UsuarioResponse alterarRole(@PathVariable Long id, @RequestBody AlterarRoleRequest request) {
        return UsuarioResponse.de(
                userService.alterarRole(usuarioAtualProvider.obterUsuarioAtual(), id, request.role())
        );
    }

    @PatchMapping("/{id}/desativar")
    public UsuarioResponse desativar(@PathVariable Long id) {
        return UsuarioResponse.de(
                userService.desativar(usuarioAtualProvider.obterUsuarioAtual(), id)
        );
    }

    @PatchMapping("/{id}/reativar")
    public UsuarioResponse reativar(@PathVariable Long id) {
        return UsuarioResponse.de(
                userService.reativar(usuarioAtualProvider.obterUsuarioAtual(), id)
        );
    }
}
