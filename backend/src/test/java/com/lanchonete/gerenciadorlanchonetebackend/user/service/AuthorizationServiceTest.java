package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServiceTest {

    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService();
    }

    @Test
    void usuarioAdminAtivoDevePoderGerenciarUsuarios() {
        UserProfile userProfile = perfil(UserRole.ADMIN);

        assertTrue(service.podeGerenciarUsuarios(userProfile));
    }

    @Test
    void usuarioAdminAtivoDevePoderAlterarRole() {
        UserProfile userProfile = perfil(UserRole.ADMIN);

        assertTrue(service.podeAlterarRole(userProfile));
    }

    @Test
    void usuarioAdminAtivoDevePoderDesativarUsuario() {
        UserProfile userProfile = perfil(UserRole.ADMIN);

        assertTrue(service.podeDesativarUsuario(userProfile));
    }

    @Test
    void usuarioClienteAtivoNaoDeveGerenciarUsuarios() {
        assertFalse(service.podeGerenciarUsuarios(perfil(UserRole.CLIENTE)));
    }

    @Test
    void usuarioFuncionarioAtivoNaoDeveAlterarRole() {
        assertFalse(service.podeAlterarRole(perfil(UserRole.FUNCIONARIO)));
    }

    @Test
    void usuarioClienteAtivoNaoDeveDesativarUsuario() {
        assertFalse(service.podeDesativarUsuario(perfil(UserRole.CLIENTE)));
    }

    @Test
    void usuarioClienteInativoNaoDeveAcessarRecursosProtegidos() {
        UserProfile userProfile = perfil(UserRole.CLIENTE);
        userProfile.desativar();

        assertFalse(service.podeAcessarRecursosProtegidos(userProfile));
    }

    @Test
    void usuarioAdminInativoNaoDeveAcessarRecursosProtegidos() {
        UserProfile userProfile = perfil(UserRole.ADMIN);
        userProfile.desativar();

        assertFalse(service.podeAcessarRecursosProtegidos(userProfile));
    }

    @Test
    void usuarioNuloNaoDeveSerAutorizado() {
        assertFalse(service.podeAcessarRecursosProtegidos(null));
    }

    @Test
    void funcionarioAtivoDevePoderOperarCozinha() {
        assertTrue(service.podeOperarCozinha(perfil(UserRole.FUNCIONARIO)));
    }

    @Test
    void adminAtivoDevePoderOperarCozinha() {
        assertTrue(service.podeOperarCozinha(perfil(UserRole.ADMIN)));
    }

    @Test
    void clienteNaoDeveOperarCozinha() {
        assertFalse(service.podeOperarCozinha(perfil(UserRole.CLIENTE)));
    }

    @Test
    void funcionarioDesativadoNaoDeveOperarCozinha() {
        UserProfile userProfile = perfil(UserRole.FUNCIONARIO);
        userProfile.desativar();

        assertFalse(service.podeOperarCozinha(userProfile));
    }

    @Test
    void adminAtivoDevePoderVerPainelAdministrativo() {
        assertTrue(service.podeVerPainelAdministrativo(perfil(UserRole.ADMIN)));
    }

    @Test
    void clienteEFuncionarioNaoDevemVerPainelAdministrativo() {
        assertFalse(service.podeVerPainelAdministrativo(perfil(UserRole.CLIENTE)));
        assertFalse(service.podeVerPainelAdministrativo(perfil(UserRole.FUNCIONARIO)));
    }

    @Test
    void adminDesativadoNaoDeveVerPainelAdministrativo() {
        UserProfile userProfile = perfil(UserRole.ADMIN);
        userProfile.desativar();

        assertFalse(service.podeVerPainelAdministrativo(userProfile));
    }

    private UserProfile perfil(UserRole role) {
        return new UserProfile("supabase-123", "Usuário", "usuario@email.com", role);
    }
}
