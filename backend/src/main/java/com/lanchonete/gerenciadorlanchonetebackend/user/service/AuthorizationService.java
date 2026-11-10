package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean podeGerenciarUsuarios(UserProfile userProfile) {
        return ehAdminAtivo(userProfile);
    }

    public boolean podeAlterarRole(UserProfile userProfile) {
        return ehAdminAtivo(userProfile);
    }

    public boolean podeDesativarUsuario(UserProfile userProfile) {
        return ehAdminAtivo(userProfile);
    }

    // Fila da cozinha e mudança de status do pedido (RF-CIC06)
    public boolean podeOperarCozinha(UserProfile userProfile) {
        return podeAcessarRecursosProtegidos(userProfile)
                && (userProfile.getRole() == UserRole.FUNCIONARIO
                    || userProfile.getRole() == UserRole.ADMIN);
    }

    public boolean podeAcessarRecursosProtegidos(UserProfile userProfile) {
        return userProfile != null && userProfile.isAtivo();
    }

    // Regra única de administrador: muda aqui, vale para todas as permissões de ADMIN
    private boolean ehAdminAtivo(UserProfile userProfile) {
        return podeAcessarRecursosProtegidos(userProfile)
                && userProfile.getRole() == UserRole.ADMIN;
    }
}