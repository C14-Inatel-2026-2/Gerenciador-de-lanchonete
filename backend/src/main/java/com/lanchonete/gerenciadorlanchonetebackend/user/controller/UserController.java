package com.lanchonete.gerenciadorlanchonetebackend.user.controller;

import com.lanchonete.gerenciadorlanchonetebackend.user.dto.AlterarRoleRequest;
import com.lanchonete.gerenciadorlanchonetebackend.user.dto.UsuarioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gerenciamento de usuários pelo ADMIN (US07, docs/api.md).
 * As regras de permissão ficam no UserService: o controller só identifica
 * quem está logado e converte entrada e saída.
 * As rotas do próprio perfil (POST /api/usuarios e /me) entram na #42.
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
}
