package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServiceTest {

    @Test
    void adminDevePoderGerenciarUsuarios() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeGerenciarUsuarios(UserRole.ADMIN);

        assertTrue(resultado);
    }

    @Test
    void clienteNaoDevePoderGerenciarUsuarios() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeGerenciarUsuarios(UserRole.CLIENTE);

        assertFalse(resultado);
    }

    @Test
    void funcionarioNaoDevePoderGerenciarUsuarios() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeGerenciarUsuarios(UserRole.FUNCIONARIO);

        assertFalse(resultado);
    }

    @Test
    void adminDevePoderAlterarRole() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeAlterarRole(UserRole.ADMIN);

        assertTrue(resultado);
    }

    @Test
    void clienteNaoDevePoderAlterarRole() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeAlterarRole(UserRole.CLIENTE);

        assertFalse(resultado);
    }

    @Test
    void funcionarioNaoDevePoderAlterarRole() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeAlterarRole(UserRole.FUNCIONARIO);

        assertFalse(resultado);
    }

    @Test
    void adminDevePoderDesativarUsuario() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeDesativarUsuario(UserRole.ADMIN);

        assertTrue(resultado);
    }

    @Test
    void clienteNaoDevePoderDesativarUsuario() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeDesativarUsuario(UserRole.CLIENTE);

        assertFalse(resultado);
    }

    @Test
    void funcionarioNaoDevePoderDesativarUsuario() {

        AuthorizationService authorizationService = new AuthorizationService();

        boolean resultado = authorizationService
                .podeDesativarUsuario(UserRole.FUNCIONARIO);

        assertFalse(resultado);
    }
}