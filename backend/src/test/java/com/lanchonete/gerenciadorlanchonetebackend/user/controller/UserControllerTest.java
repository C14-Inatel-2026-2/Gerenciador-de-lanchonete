package com.lanchonete.gerenciadorlanchonetebackend.user.controller;

import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Usa o UserService e o AuthorizationService reais: só o repositório e o
 * usuário logado são simulados, então os testes passam pela regra de permissão de verdade.
 */
@WebMvcTest(UserController.class)
@Import({UserService.class, AuthorizationService.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileRepository repository;

    @MockBean
    private UsuarioAtualProvider usuarioAtualProvider;

    private UserProfile admin;
    private UserProfile cliente;

    @BeforeEach
    void setUp() {
        admin = perfil(1L, "Admin Teste", UserRole.ADMIN);
        cliente = perfil(2L, "Cliente Teste", UserRole.CLIENTE);
    }

    private static UserProfile perfil(Long id, String nome, UserRole role) {
        UserProfile perfil = new UserProfile("supabase-" + id, nome, "usuario" + id + "@email.com", role);
        ReflectionTestUtils.setField(perfil, "id", id);
        return perfil;
    }

    @Test
    void adminListaUsuarios() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(admin, cliente));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nome").value("Cliente Teste"))
                .andExpect(jsonPath("$[1].email").value("usuario2@email.com"))
                .andExpect(jsonPath("$[1].role").value("CLIENTE"))
                .andExpect(jsonPath("$[1].ativo").value(true))
                .andExpect(jsonPath("$[1].supabaseUserId").doesNotExist());
    }

    @Test
    void clienteRecebe403AoListarUsuarios() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(cliente);

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso restrito a administradores"));
    }

    @Test
    void funcionarioRecebe403AoDesativar() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(perfil(3L, "Func", UserRole.FUNCIONARIO));

        mockMvc.perform(patch("/api/usuarios/2/desativar"))
                .andExpect(status().isForbidden());
        verify(repository, never()).save(any());
    }

    @Test
    void semUsuarioLogadoRecebe401() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual())
                .thenThrow(new UsuarioNaoAutenticadoException("Usuário não autenticado"));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminAlteraRole() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(repository.findById(2L)).thenReturn(Optional.of(cliente));
        when(repository.save(cliente)).thenReturn(cliente);

        mockMvc.perform(patch("/api/usuarios/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"FUNCIONARIO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.role").value("FUNCIONARIO"));
    }

    @Test
    void adminNaoAlteraAPropriaRole() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);

        mockMvc.perform(patch("/api/usuarios/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"CLIENTE\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Você não pode alterar a própria role"));
    }

    @Test
    void roleInvalidaRetorna400() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);

        mockMvc.perform(patch("/api/usuarios/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void roleAusenteRetorna400() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);

        mockMvc.perform(patch("/api/usuarios/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Role é obrigatória"));
    }

    @Test
    void adminDesativaUsuario() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(repository.findById(2L)).thenReturn(Optional.of(cliente));
        when(repository.save(cliente)).thenReturn(cliente);

        mockMvc.perform(patch("/api/usuarios/2/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void desativarUsuarioInexistenteRetorna404() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/usuarios/99/desativar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));
    }
}
