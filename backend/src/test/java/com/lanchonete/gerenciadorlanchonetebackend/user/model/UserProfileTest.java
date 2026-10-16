package com.lanchonete.gerenciadorlanchonetebackend.user.model;

import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileTest {

    @Test
    void novoUsuarioDeveIniciarAtivo() {
        UserProfile userProfile = new UserProfile(
                "supabase-123", "Karolina", "karolina@email.com", UserRole.CLIENTE
        );

        assertTrue(userProfile.isAtivo());
    }

    @Test
    void naoDeveCriarPerfilComRoleNula() {
        assertThrows(
                UserValidationException.class,
                () -> new UserProfile(
                        "supabase-123", "Karolina", "karolina@email.com", null
                )
        );
    }

    @Test
    void deveDesativarUsuario() {
        UserProfile userProfile = new UserProfile(
                "supabase-123", "Karolina", "karolina@email.com", UserRole.CLIENTE
        );

        assertTrue(userProfile.isAtivo());

        userProfile.desativar();

        assertFalse(userProfile.isAtivo());
    }
}
