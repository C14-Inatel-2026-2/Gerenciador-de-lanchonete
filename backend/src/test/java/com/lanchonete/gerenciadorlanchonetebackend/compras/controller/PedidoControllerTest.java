package com.lanchonete.gerenciadorlanchonetebackend.compras.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.ItemPedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.PedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.service.PedidoService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.config.SecurityConfig;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.SupabaseJwtUsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@Import({SecurityConfig.class, SupabaseJwtUsuarioAtualProvider.class, AuthorizationService.class})
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private UserProfileRepository userProfileRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void postCriaPedidoParaClienteLogadoERetorna201() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenReturn(pedido(7L));

        mockMvc.perform(post("/api/pedidos")
                        .with(jwtDoUsuario(42L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":999,"itens":[{"produtoId":10,"quantidade":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.clienteId").value(42))
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.total").value(37.8))
                .andExpect(jsonPath("$.itens[0].produtoId").value(10))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(18.9))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.itens[0].subtotal").value(37.8));

        verify(pedidoService).criarPedidoComItens(
                42L,
                new PedidoRequest(List.of(new ItemPedidoRequest(10L, 2)))
        );
    }

    @Test
    void getMeusRetornaSomentePedidosDoClienteLogado() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.listarPedidosDoCliente(42L)).thenReturn(List.of(pedido(7L)));

        mockMvc.perform(get("/api/pedidos/meus").with(jwtDoUsuario(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].clienteId").value(42))
                .andExpect(jsonPath("$[0].itens[0].nome").value("X-Burguer"));

        verify(pedidoService).listarPedidosDoCliente(42L);
    }

    @Test
    void postComProdutoInexistenteRetorna404() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        mockMvc.perform(post("/api/pedidos")
                        .with(jwtDoUsuario(42L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":99,"quantidade":1}]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Produto não encontrado"));
    }

    @Test
    void postComProdutoIndisponivelRetorna400() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenThrow(new IllegalArgumentException("O produto deve estar disponível."));

        mockMvc.perform(post("/api/pedidos")
                        .with(jwtDoUsuario(42L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":10,"quantidade":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O produto deve estar disponível."));
    }

    @Test
    void postComListaDeItensVaziaRetorna400() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), eq(new PedidoRequest(List.of()))))
                .thenThrow(new IllegalArgumentException("O pedido deve possuir pelo menos um item."));

        mockMvc.perform(post("/api/pedidos")
                        .with(jwtDoUsuario(42L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O pedido deve possuir pelo menos um item."));

        verify(pedidoService).criarPedidoComItens(42L, new PedidoRequest(List.of()));
    }

    @Test
    void postComEstoqueInsuficienteRetorna409ComNomeDoProduto() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenThrow(new EstoqueInsuficienteException(
                        "Produto X-Burger: Estoque insuficiente: disponível 1, solicitado 2"
                ));

        mockMvc.perform(post("/api/pedidos")
                        .with(jwtDoUsuario(42L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":10,"quantidade":2}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(
                        "Produto X-Burger: Estoque insuficiente: disponível 1, solicitado 2"
                ));
    }

    @Test
    void pedidoNegadoParaFuncionarioSemChamarService() throws Exception {
        logarComo(UserRole.FUNCIONARIO, 2L);

        mockMvc.perform(get("/api/pedidos/meus").with(jwtDoUsuario(2L)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void pedidoComTokenSemPerfilRetorna401() throws Exception {
        mockMvc.perform(get("/api/pedidos/meus")
                        .with(jwt().jwt(token -> token.subject("sub-sem-perfil"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Perfil não encontrado. Conclua o cadastro."));

        verifyNoInteractions(pedidoService);
    }

    @Test
    void postComTokenSemPerfilRetorna401SemCriarPedido() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .with(jwt().jwt(token -> token.subject("sub-sem-perfil")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":10,"quantidade":2}]}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Perfil não encontrado. Conclua o cadastro."));

        verify(userProfileRepository).findBySupabaseUserId("sub-sem-perfil");
        verifyNoInteractions(pedidoService);
    }

    @Test
    void postSemTokenRetorna401SemConsultarUsuarioOuCriarPedido() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":10,"quantidade":2}]}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(userProfileRepository, pedidoService);
    }

    private void logarComoCliente(Long id) {
        logarComo(UserRole.CLIENTE, id);
    }

    private void logarComo(UserRole role, Long id) {
        UserProfile usuario = mock(UserProfile.class);
        when(usuario.getId()).thenReturn(id);
        when(usuario.getRole()).thenReturn(role);
        when(usuario.isAtivo()).thenReturn(true);
        when(userProfileRepository.findBySupabaseUserId("sub-" + id))
                .thenReturn(Optional.of(usuario));
    }

    private RequestPostProcessor jwtDoUsuario(Long id) {
        return jwt().jwt(token -> token.subject("sub-" + id));
    }

    private Pedido pedido(Long id) {
        Pedido pedido = new Pedido(42L, List.of(
                new ItemPedido(10L, "X-Burguer", new BigDecimal("18.90"), 2)
        ));
        pedido.setId(id);
        return pedido;
    }
}
