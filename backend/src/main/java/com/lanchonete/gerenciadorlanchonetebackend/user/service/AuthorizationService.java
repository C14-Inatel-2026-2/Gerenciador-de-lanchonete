package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean podeGerenciarUsuarios(UserProfile userProfile) {
        return usuarioAtivoComRole(userProfile, UserRole.ADMIN);
    }

    public boolean podeAlterarRole(UserProfile userProfile) {
        return usuarioAtivoComRole(userProfile, UserRole.ADMIN);
    }

    public boolean podeDesativarUsuario(UserProfile userProfile) {
        return usuarioAtivoComRole(userProfile, UserRole.ADMIN);
    }

    private boolean usuarioAtivoComRole(
            UserProfile userProfile,
            UserRole role
    ) {
        return userProfile != null
                && userProfile.isAtivo()
                && userProfile.getRole() == role;
    }
}
