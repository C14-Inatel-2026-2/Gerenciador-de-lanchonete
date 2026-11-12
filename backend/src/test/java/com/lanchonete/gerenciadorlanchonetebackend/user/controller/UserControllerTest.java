package com.lanchonete.gerenciadorlanchonetebackend.user.controller;

import com.lanchonete.gerenciadorlanchonetebackend.config.SecurityConfig;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AdministradoresIniciais;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Usa o UserService, o AuthorizationService e o SecurityConfig reais: só o
 * repositório, o usuário logado e o decodificador do token são simulados.
 * jwt() simula um token válido do Supabase; sem ele, a requisição vai sem token.
 */
@WebMvcTest(UserController.class)
@Import({UserService.class, AuthorizationService.class, AdministradoresIniciais.class, SecurityConfig.class})
@TestPropertySource(properties = "app.admin.emails=admin@admin.com")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileRepository repository;

    @MockBean
    private UsuarioAtualProvider usuarioAtualProvider;

    @MockBean
    private JwtDecoder jwtDecoder;

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

    // ---- token (SecurityConfig)

    @Test
    void semTokenRecebe401SemChegarAoController() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(usuarioAtualProvider, repository);
    }

    @Test
    void tokenInvalidoOuExpiradoRecebe401() throws Exception {
        when(jwtDecoder.decode("token-expirado")).thenThrow(new BadJwtException("Jwt expired"));

        mockMvc.perform(get("/api/usuarios/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-expirado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(usuarioAtualProvider);
    }

    // ---- cadastro e perfil próprio (US01, #42)

    @Test
    void cadastroCriaClienteComIdEEmailDoTokenIgnorandoRoleDoCorpo() throws Exception {
        when(repository.save(any(UserProfile.class))).thenAnswer(chamada -> chamada.getArgument(0));

        mockMvc.perform(post("/api/usuarios")
                        .with(jwt().jwt(token -> token.subject("supabase-nova").claim("email", "Nova@Email.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nova Cliente\",\"role\":\"ADMIN\",\"email\":\"outro@email.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Nova Cliente"))
                .andExpect(jsonPath("$.email").value("nova@email.com"))
                .andExpect(jsonPath("$.role").value("CLIENTE"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void cadastroComEmailDeAdminInicialCriaAdmin() throws Exception {
        when(repository.save(any(UserProfile.class))).thenAnswer(chamada -> chamada.getArgument(0));

        mockMvc.perform(post("/api/usuarios")
                        .with(jwt().jwt(token -> token.subject("supabase-admin").claim("email", "admin@admin.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Administrador\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void cadastroDePerfilQueJaExisteRetorna409() throws Exception {
        when(repository.existsBySupabaseUserId("supabase-2")).thenReturn(true);

        mockMvc.perform(post("/api/usuarios")
                        .with(jwt().jwt(token -> token.subject("supabase-2").claim("email", "usuario2@email.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Cliente Teste\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("E-mail já cadastrado"));

        verify(repository, never()).save(any());
    }

    @Test
    void cadastroSemTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sem Token\"}"))
                .andExpect(status().isUnauthorized());

        verify(repository, never()).save(any());
    }

    @Test
    void meuPerfilRetornaUsuarioDoToken() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(cliente);

        mockMvc.perform(get("/api/usuarios/me").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Cliente Teste"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void atualizarMeuPerfilMudaSoONome() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(cliente);
        when(repository.save(cliente)).thenReturn(cliente);

        mockMvc.perform(patch("/api/usuarios/me")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nome Novo\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void atualizarMeuPerfilComNomeEmBrancoRetorna400() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(cliente);

        mockMvc.perform(patch("/api/usuarios/me")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Nome é obrigatório"));

        verify(repository, never()).save(any());
    }

    // ---- gerenciamento pelo ADMIN (US07, #43)

    @Test
    void adminListaUsuarios() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(admin, cliente));

        mockMvc.perform(get("/api/usuarios").with(jwt()))
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

        mockMvc.perform(get("/api/usuarios").with(jwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso restrito a administradores"));
    }

    @Test
    void funcionarioRecebe403AoDesativar() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(perfil(3L, "Func", UserRole.FUNCIONARIO));

        mockMvc.perform(patch("/api/usuarios/2/desativar").with(jwt()))
                .andExpect(status().isForbidden());
        verify(repository, never()).save(any());
    }

    @Test
    void semUsuarioLogadoRecebe401() throws Exception {
        // Token válido, mas sem perfil no nosso banco
        when(usuarioAtualProvider.obterUsuarioAtual())
                .thenThrow(new UsuarioNaoAutenticadoException("Usuário não autenticado"));

        mockMvc.perform(get("/api/usuarios").with(jwt()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminAlteraRole() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(repository.findById(2L)).thenReturn(Optional.of(cliente));
        when(repository.save(cliente)).thenReturn(cliente);

        mockMvc.perform(patch("/api/usuarios/2/role")
                        .with(jwt())
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
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"CLIENTE\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Você não pode alterar a própria role"));
    }

    @Test
    void roleInvalidaRetorna400() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);

        mockMvc.perform(patch("/api/usuarios/2/role")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void roleAusenteRetorna400() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);

        mockMvc.perform(patch("/api/usuarios/2/role")
                        .with(jwt())
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

        mockMvc.perform(patch("/api/usuarios/2/desativar").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void desativarUsuarioInexistenteRetorna404() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/usuarios/99/desativar").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));
    }
}
