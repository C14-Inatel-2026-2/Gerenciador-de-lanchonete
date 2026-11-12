package com.lanchonete.gerenciadorlanchonetebackend.config;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller.CategoriaController;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller.ProdutoController;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.CategoriaService;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Quais rotas são públicas e quais exigem token (#55), com o SecurityConfig real.
 * Os testes de Categoria e Produto usam MockMvc standalone (sem os filtros de
 * segurança), então é aqui que fica provado que o catálogo está protegido.
 */
@WebMvcTest(controllers = {CategoriaController.class, ProdutoController.class})
@Import({SecurityConfig.class, AuthorizationService.class})
class SecurityConfigTest {

    private static final String JSON = "{ \"nome\": \"Lanches\" }";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private ProdutoService produtoService;

    @MockBean
    private UsuarioAtualProvider usuarioAtualProvider;

    @MockBean
    private JwtDecoder jwtDecoder;

    // ---- públicas: o cardápio abre sem login

    @Test
    void listarCategoriasEPublico() throws Exception {
        mockMvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    @Test
    void listarProdutosEPublico() throws Exception {
        mockMvc.perform(get("/api/produtos")).andExpect(status().isOk());
    }

    @Test
    void cardapioEPublico() throws Exception {
        mockMvc.perform(get("/api/produtos/cardapio")).andExpect(status().isOk());
    }

    // ---- fechadas: alterar o catálogo exige token

    @Test
    void criarCategoriaSemTokenRetorna401SemChamarOService() throws Exception {
        mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(categoriaService, usuarioAtualProvider);
    }

    @Test
    void editarCategoriaSemTokenRetorna401() throws Exception {
        mockMvc.perform(put("/api/categorias/1").contentType(MediaType.APPLICATION_JSON).content(JSON))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(categoriaService);
    }

    @Test
    void desativarProdutoSemTokenRetorna401() throws Exception {
        mockMvc.perform(patch("/api/produtos/1/desativar"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(produtoService);
    }

    @Test
    void tokenInvalidoRetorna401SemChamarOService() throws Exception {
        when(jwtDecoder.decode("token-falso")).thenThrow(new BadJwtException("assinatura inválida"));

        mockMvc.perform(post("/api/categorias")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-falso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(categoriaService, usuarioAtualProvider);
    }

    @Test
    void headerAntigoXUsuarioIdNaoAutentica() throws Exception {
        mockMvc.perform(post("/api/categorias")
                .header("X-Usuario-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(categoriaService, usuarioAtualProvider);
    }
}
