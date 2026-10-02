package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean podeGerenciarUsuarios(UserProfile userProfile) {
        return podeAcessarRecursosProtegidos(userProfile)
                && userProfile.getRole() == UserRole.ADMIN;
    }

    public boolean podeAlterarRole(UserProfile userProfile) {
        return podeAcessarRecursosProtegidos(userProfile)
                && userProfile.getRole() == UserRole.ADMIN;
    }

    public boolean podeDesativarUsuario(UserProfile userProfile) {
        return podeAcessarRecursosProtegidos(userProfile)
                && userProfile.getRole() == UserRole.ADMIN;
    }

    public boolean podeAcessarRecursosProtegidos(UserProfile userProfile) {
        return userProfile != null && userProfile.isAtivo();
    }
}