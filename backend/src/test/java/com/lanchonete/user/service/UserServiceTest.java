package com.lanchonete.user.service;

import com.lanchonete.user.model.UserProfile;
import com.lanchonete.user.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserServiceTest {

    @Test
    void deveCriarNovoUsuarioComoCliente() {

        // Arrange
        UserService userService = new UserService();

        // Act
        UserProfile userProfile = userService.criarPerfil(
                "supabase-123",
                "Karolina",
                "karolina@email.com"
        );

        // Assert
        assertEquals(UserRole.CLIENTE, userProfile.getRole());
    }
}