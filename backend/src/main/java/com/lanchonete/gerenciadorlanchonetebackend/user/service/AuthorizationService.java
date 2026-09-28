package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;

public class AuthorizationService {

    public boolean podeGerenciarUsuarios(UserRole role) {
        return role == UserRole.ADMIN;
    }

    public boolean podeAlterarRole(UserRole role) {
        return role == UserRole.ADMIN;
    }

    public boolean podeDesativarUsuario(UserRole role) {
        return role == UserRole.ADMIN;
    }
}