package com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.CategoriaService;
import com.lanchonete.gerenciadorlanchonetebackend.config.GlobalExceptionHandler;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {
    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioAtualProvider usuarioAtualProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        CategoriaController controller = new CategoriaController(
                categoriaService,
                usuarioAtualProvider,
                new AuthorizationService()
        );
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveListarCategoriasAtivas() throws Exception {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        when(categoriaRepository.findByAtivoTrue()).thenReturn(List.of(categoria));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Lanches"))
                .andExpect(jsonPath("$[0].ativo").value(true));

        verify(categoriaRepository).findByAtivoTrue();
    }

    @Test
    void deveCriarCategoriaQuandoUsuarioForAdmin() throws Exception {
        UserProfile admin = new UserProfile("admin-1", "Admin", "admin@example.com", UserRole.ADMIN);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(categoriaRepository.existsByNomeIgnoreCase("Lanches")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(2L);
            return categoria;
        });

        mockMvc.perform(post("/api/categorias")
                        .contentType("application/json")
                        .content("{\"nome\":\"Lanches\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Lanches"))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(categoriaRepository).existsByNomeIgnoreCase("Lanches");
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void naoDeveCriarCategoriaQuandoUsuarioNaoForAdmin() throws Exception {
        UserProfile cliente = new UserProfile("cliente-1", "Cliente", "cliente@example.com", UserRole.CLIENTE);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(cliente);

        mockMvc.perform(post("/api/categorias")
                        .contentType("application/json")
                        .content("{\"nome\":\"Lanches\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Apenas administradores podem criar categorias"));

        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void deveEditarCategoriaQuandoUsuarioForAdmin() throws Exception {
        UserProfile admin = new UserProfile("admin-1", "Admin", "admin@example.com", UserRole.ADMIN);
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(categoriaRepository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType("application/json")
                        .content("{\"nome\":\"Lanches\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Lanches"));

        verify(categoriaRepository).existsByNomeIgnoreCaseAndIdNot("Lanches", 1L);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void naoDeveEditarCategoriaQuandoUsuarioNaoForAdmin() throws Exception {
        UserProfile cliente = new UserProfile("cliente-1", "Cliente", "cliente@example.com", UserRole.CLIENTE);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(cliente);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType("application/json")
                        .content("{\"nome\":\"Lanches\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Apenas administradores podem gerenciar categorias"));

        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void deveDesativarCategoriaQuandoUsuarioForAdmin() throws Exception {
        UserProfile admin = new UserProfile("admin-1", "Admin", "admin@example.com", UserRole.ADMIN);
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(admin);
        when(categoriaRepository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        mockMvc.perform(patch("/api/categorias/1/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ativo").value(false));

        verify(categoriaRepository).save(categoria);
    }

    @Test
    void naoDeveDesativarCategoriaQuandoUsuarioNaoForAdmin() throws Exception {
        UserProfile cliente = new UserProfile("cliente-1", "Cliente", "cliente@example.com", UserRole.CLIENTE);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(cliente);

        mockMvc.perform(patch("/api/categorias/1/desativar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Apenas administradores podem gerenciar categorias"));

        verifyNoInteractions(categoriaRepository);
    }
}
