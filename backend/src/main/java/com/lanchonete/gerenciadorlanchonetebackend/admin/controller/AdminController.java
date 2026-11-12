package com.lanchonete.gerenciadorlanchonetebackend.admin.controller;

import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.ResumoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.admin.service.AdminService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Painel administrativo (US05 · #48, docs/api.md). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UsuarioAtualProvider usuarioAtualProvider;
    private final AuthorizationService authorizationService;

    public AdminController(
            AdminService adminService,
            UsuarioAtualProvider usuarioAtualProvider,
            AuthorizationService authorizationService
    ) {
        this.adminService = adminService;
        this.usuarioAtualProvider = usuarioAtualProvider;
        this.authorizationService = authorizationService;
    }

    // Sem token → 401 no SecurityConfig; sem perfil → 401 no provider; não-admin → 403 aqui.
    @GetMapping("/resumo")
    public ResumoResponse resumo() {
        UserProfile usuario = usuarioAtualProvider.obterUsuarioAtual();
        if (!authorizationService.podeVerPainelAdministrativo(usuario)) {
            throw new AcessoNegadoException("Acesso restrito a administradores");
        }
        return adminService.obterResumo();
    }
}
