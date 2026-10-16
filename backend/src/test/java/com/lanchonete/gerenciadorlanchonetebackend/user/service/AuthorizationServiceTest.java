package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServiceTest {

    @Test
    void usuarioAdminAtivoDevePoderGerenciarUsuarios() {
        UserProfile userProfile = perfil(UserRole.ADMIN);

        assertTrue(new AuthorizationService().podeGerenciarUsuarios(userProfile));
    }

    @Test
    void usuarioAdminAtivoDevePoderAlterarRole() {
        UserProfile userProfile = perfil(UserRole.ADMIN);

        assertTrue(new AuthorizationService().podeAlterarRole(userProfile));
    }

    @Test
    void usuarioAdminAtivoDevePoderDesativarUsuario() {
        UserProfile userProfile = perfil(UserRole.ADMIN);

        assertTrue(new AuthorizationService().podeDesativarUsuario(userProfile));
    }

    @Test
    void usuarioClienteAtivoNaoDeveGerenciarUsuarios() {
        assertFalse(new AuthorizationService().podeGerenciarUsuarios(perfil(UserRole.CLIENTE)));
    }

    @Test
    void usuarioFuncionarioAtivoNaoDeveAlterarRole() {
        assertFalse(new AuthorizationService().podeAlterarRole(perfil(UserRole.FUNCIONARIO)));
    }

    @Test
    void usuarioClienteAtivoNaoDeveDesativarUsuario() {
        assertFalse(new AuthorizationService().podeDesativarUsuario(perfil(UserRole.CLIENTE)));
    }

    @Test
    void usuarioClienteInativoNaoDeveAcessarRecursosProtegidos() {
        UserProfile userProfile = perfil(UserRole.CLIENTE);
        userProfile.desativar();

        assertFalse(new AuthorizationService().podeAcessarRecursosProtegidos(userProfile));
    }

    @Test
    void usuarioAdminInativoNaoDeveAcessarRecursosProtegidos() {
        UserProfile userProfile = perfil(UserRole.ADMIN);
        userProfile.desativar();

        assertFalse(new AuthorizationService().podeAcessarRecursosProtegidos(userProfile));
    }

    @Test
    void usuarioNuloNaoDeveSerAutorizado() {
        assertFalse(new AuthorizationService().podeAcessarRecursosProtegidos(null));
    }

    private UserProfile perfil(UserRole role) {
        return new UserProfile("supabase-123", "Usuário", "usuario@email.com", role);
    }
}
