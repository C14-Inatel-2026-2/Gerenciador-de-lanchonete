package com.lanchonete.gerenciadorlanchonetebackend.user.dto;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;

/**
 * Perfil devolvido pela API (docs/api.md). Não expõe a entidade nem o
 * supabaseUserId.
 */
public record UsuarioResponse(Long id, String nome, String email, UserRole role, boolean ativo) {

    public static UsuarioResponse de(UserProfile usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.isAtivo()
        );
    }
}
