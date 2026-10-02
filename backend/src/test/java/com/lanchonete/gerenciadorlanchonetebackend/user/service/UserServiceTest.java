package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileRepository repository;

    @Test
    void deveSalvarNovoPerfilQuandoEmailNaoExiste() {
        when(repository.existsByEmail("karolina@email.com")).thenReturn(false);
        UserService userService = new UserService(repository);

        userService.criarPerfil(
                "supabase-123", "Karolina", " Karolina@EMAIL.com "
        );

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(repository).save(captor.capture());
        UserProfile savedProfile = captor.getValue();
        assertEquals(UserRole.CLIENTE, savedProfile.getRole());
        assertEquals("Karolina", savedProfile.getNome());
        assertEquals("karolina@email.com", savedProfile.getEmail());
    }

    @Test
    void naoDeveCriarUsuarioComEmailDuplicado() {
        when(repository.existsByEmail("karolina@email.com")).thenReturn(true);
        UserService userService = new UserService(repository);

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(
                        "supabase-123", "Karolina", "Karolina@email.com"
                )
        );

        verify(repository, never()).save(any(UserProfile.class));
    }

    @Test
    void naoDeveCriarUsuarioComEmailNulo() {
        assertInvalid(null, "Karolina", "karolina@email.com");
    }

    @Test
    void naoDeveCriarUsuarioComEmailEmBranco() {
        assertInvalid("supabase-123", "Karolina", " ");
    }

    @Test
    void naoDeveCriarUsuarioComNomeNulo() {
        assertInvalid("supabase-123", null, "karolina@email.com");
    }

    @Test
    void naoDeveCriarUsuarioComNomeEmBranco() {
        assertInvalid("supabase-123", " ", "karolina@email.com");
    }

    @Test
    void naoDeveCriarUsuarioComSupabaseUserIdNulo() {
        assertInvalid(null, "Karolina", "karolina@email.com");
    }

    @Test
    void naoDeveCriarUsuarioComSupabaseUserIdEmBranco() {
        assertInvalid(" ", "Karolina", "karolina@email.com");
    }

    @Test
    void naoDeveCriarUsuarioComEmailInvalido() {
        assertInvalid("supabase-123", "Karolina", "email-invalido");
    }

    private void assertInvalid(String supabaseUserId, String nome, String email) {
        UserService userService = new UserService(repository);

        assertThrows(
                UserValidationException.class,
                () -> userService.criarPerfil(supabaseUserId, nome, email)
        );
        verify(repository, never()).save(any(UserProfile.class));
    }
}
