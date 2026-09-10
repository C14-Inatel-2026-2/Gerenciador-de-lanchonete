package com.lanchonete.user.service;

import com.lanchonete.user.exception.UserValidationException;
import com.lanchonete.user.model.UserProfile;
import com.lanchonete.user.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    @Test
    void deveCriarNovoUsuarioComoCliente() {

        UserService userService = new UserService();

        UserProfile userProfile = userService.criarPerfil(
                "supabase-123",
                "Karolina",
                "karolina@email.com"
        );

        assertEquals(UserRole.CLIENTE, userProfile.getRole());
    }

    @Test
    void naoDeveCriarUsuarioSemSupabaseUserId() {

        UserService userService = new UserService();

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "",
                        "Karolina",
                        "karolina@email.com"
                )
        );
    }

    @Test
    void naoDeveCriarUsuarioSemNome() {

    
        UserService userService = new UserService();

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123",
                        "",
                        "karolina@email.com"
                )
        );
    }

    @Test
    void naoDeveCriarUsuarioSemEmail() {

        UserService userService = new UserService();

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123",
                        "Karolina",
                        ""
                )
        );
    }

    @Test
    void naoDeveCriarUsuarioComEmailInvalido() {

   
        UserService userService = new UserService();

     
        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123",
                        "Karolina",
                        "email-invalido"
                )
        );
    }
}