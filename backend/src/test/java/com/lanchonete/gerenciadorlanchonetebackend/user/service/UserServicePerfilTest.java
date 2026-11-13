package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.exception.EmailJaCadastradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Primeiro administrador (ADMIN_EMAILS, #55) e perfil próprio (US01, #42). Sem mock do AdministradoresIniciais. */
@ExtendWith(MockitoExtension.class)
class UserServicePerfilTest {

    @Mock
    private UserProfileRepository repository;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, new AuthorizationService(),
                new AdministradoresIniciais(" Admin@Admin.com , dono@lanchonete.com"));
    }

    // ---- primeiro administrador

    @Test
    void emailDeAdminEmailsNasceAdmin() {
        when(repository.save(any(UserProfile.class))).thenAnswer(chamada -> chamada.getArgument(0));

        UserProfile perfil = service.criarPerfil("sub-admin", "Administrador", "ADMIN@admin.com");

        assertEquals(UserRole.ADMIN, perfil.getRole());
    }

    @Test
    void outroEmailNasceCliente() {
        when(repository.save(any(UserProfile.class))).thenAnswer(chamada -> chamada.getArgument(0));

        UserProfile perfil = service.criarPerfil("sub-cliente", "Cliente", "cliente@email.com");

        assertEquals(UserRole.CLIENTE, perfil.getRole());
    }

    @Test
    void semAdminEmailsTodoMundoNasceCliente() {
        UserService semAdmins = new UserService(repository, new AuthorizationService());
        when(repository.save(any(UserProfile.class))).thenAnswer(chamada -> chamada.getArgument(0));

        assertEquals(UserRole.CLIENTE, semAdmins.criarPerfil("sub-admin", "Admin", "admin@admin.com").getRole());
    }

    @Test
    void naoCriaSegundoPerfilParaAMesmaContaDoSupabase() {
        when(repository.existsBySupabaseUserId("sub-123")).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class,
                () -> service.criarPerfil("sub-123", "Karolina", "novo@email.com"));
        verify(repository, never()).save(any());
    }

    // ---- atualizar o próprio nome

    @Test
    void atualizaNomeSemMudarARole() {
        UserProfile cliente = new UserProfile("sub-123", "Antigo", "cliente@email.com", UserRole.CLIENTE);
        when(repository.save(cliente)).thenReturn(cliente);

        UserProfile atualizado = service.atualizarNome(cliente, "  Nome Novo ");

        assertEquals("Nome Novo", atualizado.getNome());
        assertEquals(UserRole.CLIENTE, atualizado.getRole());
    }

    @Test
    void naoAtualizaParaNomeEmBranco() {
        UserProfile cliente = new UserProfile("sub-123", "Antigo", "cliente@email.com", UserRole.CLIENTE);

        assertThrows(UserValidationException.class, () -> service.atualizarNome(cliente, " "));
        assertEquals("Antigo", cliente.getNome());
        verify(repository, never()).save(any());
    }
}
