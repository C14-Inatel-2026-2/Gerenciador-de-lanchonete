package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private UserService userService;

    @Test
    void deveCriarNovoUsuarioComoCliente() {
        UserService userService = new UserService(new RepositorioDeTeste());

        UserProfile userProfile = userService.criarPerfil(
                "supabase-123", "Karolina", "karolina@email.com"
        );

        assertEquals(UserRole.CLIENTE, userProfile.getRole());
        assertTrue(userProfile.isAtivo());
    }

    @Test
    void naoDeveCriarUsuarioComEmailInvalido() {
        UserService userService = new UserService(new RepositorioDeTeste());

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123", "Karolina", "email-invalido"
                )
        );
    }

    @Test
    void naoDeveCriarUsuarioComEmailDuplicado() {
        when(repository.existsByEmail("karolina@email.com")).thenReturn(true);

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123", "Karolina", "karolina@email.com"
                )
        );
        verify(repository, never()).save(any(UserProfile.class));
    }

    @Test
    void deveSalvarNovoPerfilQuandoEmailNaoExiste() {
        when(repository.existsByEmail("karolina@email.com")).thenReturn(false);

        userService.criarPerfil(
                "supabase-123", "Karolina", "karolina@email.com"
        );

        verify(repository).save(any(UserProfile.class));
    }

    @Test
    void naoDeveCriarUsuarioSemSupabaseUserId() {
        UserService userService = new UserService(new RepositorioDeTeste());

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "", "Karolina", "karolina@email.com"
                )
        );
    }

    @Test
    void naoDeveCriarUsuarioSemNome() {
        UserService userService = new UserService(new RepositorioDeTeste());

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123", "", "karolina@email.com"
                )
        );
    }

    @Test
    void naoDeveCriarUsuarioSemEmail() {
        UserService userService = new UserService(new RepositorioDeTeste());

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123", "Karolina", ""
                )
        );
    }

    private static class RepositorioDeTeste implements UserProfileRepository {
        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public UserProfile save(UserProfile userProfile) {
            return userProfile;
        }
    }
}
