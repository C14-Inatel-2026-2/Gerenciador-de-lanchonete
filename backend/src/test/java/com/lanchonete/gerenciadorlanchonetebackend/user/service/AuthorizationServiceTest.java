package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServiceTest {

    @Test
    void usuarioAdminAtivoDevePoderGerenciarUsuarios() {
        AuthorizationService authorizationService = new AuthorizationService();
        UserProfile userProfile = new UserProfile(
                "supabase-123", "Admin", "admin@email.com", UserRole.ADMIN
        );

        assertTrue(authorizationService.podeGerenciarUsuarios(userProfile));
    }

    @Test
    void usuarioAdminInativoNaoDeveAcessarRecursosProtegidos() {
        AuthorizationService authorizationService = new AuthorizationService();
        UserProfile userProfile = new UserProfile(
                "supabase-123", "Admin", "admin@email.com", UserRole.ADMIN
        );
        userProfile.desativar();

        assertFalse(authorizationService.podeGerenciarUsuarios(userProfile));
    }

    @Test
    void usuarioClienteAtivoNaoDeveGerenciarUsuarios() {
        AuthorizationService authorizationService = new AuthorizationService();
        UserProfile userProfile = new UserProfile(
                "supabase-123", "Cliente", "cliente@email.com", UserRole.CLIENTE
        );

        assertFalse(authorizationService.podeGerenciarUsuarios(userProfile));
    }

    @Test
    void usuarioFuncionarioAtivoNaoDeveAlterarRole() {
        AuthorizationService authorizationService = new AuthorizationService();
        UserProfile userProfile = new UserProfile(
                "supabase-123", "Funcionario", "funcionario@email.com",
                UserRole.FUNCIONARIO
        );

        assertFalse(authorizationService.podeAlterarRole(userProfile));
    }

    @Test
    void usuarioClienteAtivoNaoDeveDesativarUsuario() {
        AuthorizationService authorizationService = new AuthorizationService();
        UserProfile userProfile = new UserProfile(
                "supabase-123", "Cliente", "cliente@email.com", UserRole.CLIENTE
        );

        assertFalse(authorizationService.podeDesativarUsuario(userProfile));
    }

    @Test
    void usuarioNuloNaoDeveSerAutorizado() {
        AuthorizationService authorizationService = new AuthorizationService();

        assertFalse(authorizationService.podeGerenciarUsuarios(null));
    }
}
