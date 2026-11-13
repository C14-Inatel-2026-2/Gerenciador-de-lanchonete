package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Gerenciamento de usuários pelo ADMIN (US07, #43). */
@ExtendWith(MockitoExtension.class)
class UserServiceGerenciamentoTest {

    @Mock
    private UserProfileRepository repository;

    private UserService service;
    private UserProfile admin;
    private UserProfile cliente;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, new AuthorizationService());
        admin = perfil(1L, "Admin", UserRole.ADMIN);
        cliente = perfil(2L, "Cliente", UserRole.CLIENTE);
    }

    private static UserProfile perfil(Long id, String nome, UserRole role) {
        UserProfile perfil = new UserProfile("supabase-" + id, nome, nome.toLowerCase() + "@email.com", role);
        ReflectionTestUtils.setField(perfil, "id", id);
        return perfil;
    }

    // ---- listar

    @Test
    void adminListaTodosOsUsuarios() {
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(admin, cliente));

        assertEquals(List.of(admin, cliente), service.listar(admin));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CLIENTE", "FUNCIONARIO"})
    void naoAdminNaoListaUsuarios(UserRole role) {
        UserProfile solicitante = perfil(5L, "Outro", role);

        assertThrows(AcessoNegadoException.class, () -> service.listar(solicitante));
        verify(repository, never()).findAllByOrderByNomeAsc();
    }

    @Test
    void adminDesativadoNaoListaUsuarios() {
        admin.desativar();

        assertThrows(AcessoNegadoException.class, () -> service.listar(admin));
    }

    // ---- alterarRole

    @Test
    void adminPromoveClienteAFuncionario() {
        when(repository.findById(2L)).thenReturn(Optional.of(cliente));
        when(repository.save(cliente)).thenReturn(cliente);

        UserProfile alterado = service.alterarRole(admin, 2L, UserRole.FUNCIONARIO);

        assertEquals(UserRole.FUNCIONARIO, alterado.getRole());
        verify(repository).save(cliente);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CLIENTE", "FUNCIONARIO"})
    void naoAdminNaoAlteraRole(UserRole role) {
        UserProfile solicitante = perfil(5L, "Outro", role);

        assertThrows(AcessoNegadoException.class,
                () -> service.alterarRole(solicitante, 2L, UserRole.ADMIN));
        verify(repository, never()).save(any());
    }

    @Test
    void adminNaoAlteraAPropriaRole() {
        AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                () -> service.alterarRole(admin, 1L, UserRole.CLIENTE));

        assertEquals("Você não pode alterar a própria role", ex.getMessage());
        assertEquals(UserRole.ADMIN, admin.getRole());
        verify(repository, never()).save(any());
    }

    @Test
    void naoAlteraRoleParaNula() {
        assertThrows(UserValidationException.class, () -> service.alterarRole(admin, 2L, null));
        verify(repository, never()).save(any());
    }

    @Test
    void alterarRoleDeUsuarioInexistenteRetornaNaoEncontrado() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.alterarRole(admin, 99L, UserRole.FUNCIONARIO));
    }

    // ---- desativar

    @Test
    void adminDesativaUsuarioQueContinuaNoBanco() {
        when(repository.findById(2L)).thenReturn(Optional.of(cliente));
        when(repository.save(cliente)).thenReturn(cliente);

        UserProfile desativado = service.desativar(admin, 2L);

        assertFalse(desativado.isAtivo());
        verify(repository).save(cliente); // salvo como inativo, não apagado
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CLIENTE", "FUNCIONARIO"})
    void naoAdminNaoDesativaUsuario(UserRole role) {
        UserProfile solicitante = perfil(5L, "Outro", role);

        assertThrows(AcessoNegadoException.class, () -> service.desativar(solicitante, 2L));
        verify(repository, never()).save(any());
    }

    @Test
    void adminNaoDesativaASiMesmo() {
        assertThrows(AcessoNegadoException.class, () -> service.desativar(admin, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void desativarUsuarioInexistenteRetornaNaoEncontrado() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.desativar(admin, 99L));
    }

    // ---- reativar

    @Test
    void adminReativaUsuarioDesativadoComOMesmoPerfil() {
        cliente.desativar();
        when(repository.findById(2L)).thenReturn(Optional.of(cliente));
        when(repository.save(cliente)).thenReturn(cliente);

        UserProfile reativado = service.reativar(admin, 2L);

        assertTrue(reativado.isAtivo());
        assertEquals(UserRole.CLIENTE, reativado.getRole());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"CLIENTE", "FUNCIONARIO"})
    void naoAdminNaoReativaUsuario(UserRole role) {
        UserProfile solicitante = perfil(5L, "Outro", role);

        assertThrows(AcessoNegadoException.class, () -> service.reativar(solicitante, 2L));
        verify(repository, never()).save(any());
    }

    @Test
    void adminDesativadoNaoReativaNinguem() {
        // Inclusive a si mesmo: quem está desativado não usa as rotas protegidas
        admin.desativar();

        assertThrows(AcessoNegadoException.class, () -> service.reativar(admin, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void reativarUsuarioInexistenteRetornaNaoEncontrado() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.reativar(admin, 99L));
    }
}
