package com.lanchonete.gerenciadorlanchonetebackend.compras.controller;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.TransicaoStatusInvalidaException;
import com.lanchonete.gerenciadorlanchonetebackend.compras.service.PedidoLifecycleService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Teste de controller: sobe só a camada web (controller + GlobalExceptionHandler),
// sem banco. O service e o usuário logado são mocks; a regra de permissão é a real.
@WebMvcTest(CozinhaController.class)
@Import(AuthorizationService.class)
class CozinhaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoLifecycleService lifecycleService;

    @MockBean
    private UsuarioAtualProvider usuarioAtualProvider;

    @Test
    void filaRetornaPedidosNoFormatoDoContratoParaFuncionario() throws Exception {
        logarComo(UserRole.FUNCIONARIO);
        when(lifecycleService.listarFila()).thenReturn(List.of(pedido(5L), pedido(6L)));

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(5))
            .andExpect(jsonPath("$[0].clienteId").value(1))
            .andExpect(jsonPath("$[0].status").value("CRIADO"))
            .andExpect(jsonPath("$[0].total").value(25.0))
            .andExpect(jsonPath("$[0].criadoEm").exists())
            .andExpect(jsonPath("$[0].itens[0].nome").value("Hamburguer"))
            .andExpect(jsonPath("$[0].itens[0].subtotal").value(25.0))
            .andExpect(jsonPath("$[1].id").value(6));
    }

    @Test
    void filaPermitidaParaAdmin() throws Exception {
        logarComo(UserRole.ADMIN);
        when(lifecycleService.listarFila()).thenReturn(List.of());

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void filaNegadaParaClienteSemConsultarPedidos() throws Exception {
        logarComo(UserRole.CLIENTE);

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.erro").value("Acesso restrito a funcionários e administradores"));

        verifyNoInteractions(lifecycleService);
    }

    @Test
    void filaSemUsuarioIdentificadoRetorna401() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual())
            .thenThrow(new UsuarioNaoAutenticadoException("Usuário não autenticado"));

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(lifecycleService);
    }

        @Test
    void patchStatusAvancaPedidoERetornaPedidoAtualizado() throws Exception {
        logarComo(UserRole.FUNCIONARIO);
        Pedido emPreparo = pedido(5L);
        emPreparo.alterarStatus(StatusPedido.EM_PREPARO);
        when(lifecycleService.atualizarStatusPorId(5L, StatusPedido.EM_PREPARO)).thenReturn(emPreparo);

        mockMvc.perform(patchStatus(5L, "{ \"status\": \"EM_PREPARO\" }"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }

    @Test
    void patchComTransicaoInvalidaRetorna409() throws Exception {
        logarComo(UserRole.FUNCIONARIO);
        when(lifecycleService.atualizarStatusPorId(5L, StatusPedido.ENTREGUE))
            .thenThrow(new TransicaoStatusInvalidaException(StatusPedido.CRIADO, StatusPedido.ENTREGUE));

        mockMvc.perform(patchStatus(5L, "{ \"status\": \"ENTREGUE\" }"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.erro").value("Transição de status inválida: CRIADO -> ENTREGUE."));
    }

    @Test
    void patchDePedidoInexistenteRetorna404() throws Exception {
        logarComo(UserRole.ADMIN);
        when(lifecycleService.atualizarStatusPorId(99L, StatusPedido.EM_PREPARO))
            .thenThrow(new RecursoNaoEncontradoException("Pedido não encontrado"));

        mockMvc.perform(patchStatus(99L, "{ \"status\": \"EM_PREPARO\" }"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.erro").value("Pedido não encontrado"));
    }

    @Test
    void patchComStatusForaDoEnumRetorna400() throws Exception {
        logarComo(UserRole.FUNCIONARIO);

        mockMvc.perform(patchStatus(5L, "{ \"status\": \"VOANDO\" }"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").value("Requisição inválida"));

        verifyNoInteractions(lifecycleService);
    }

    @Test
    void patchSemStatusRetorna400() throws Exception {
        logarComo(UserRole.FUNCIONARIO);

        mockMvc.perform(patchStatus(5L, "{ }"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").value("O novo status não pode ser nulo."));

        verifyNoInteractions(lifecycleService);
    }

    @Test
    void patchNegadoParaClienteSemAlterarPedido() throws Exception {
        logarComo(UserRole.CLIENTE);

        mockMvc.perform(patchStatus(5L, "{ \"status\": \"EM_PREPARO\" }"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(lifecycleService);
    }

    private RequestBuilder patchStatus(Long id, String json) {
        return patch("/api/pedidos/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);
    }

    private void logarComo(UserRole role) {
        UserProfile usuario = new UserProfile("teste-" + role, "Usuário Teste", "teste@lanchonete.com", role);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(usuario);
    }

    private Pedido pedido(Long id) {
        Pedido pedido = new Pedido(1L, List.of(new ItemPedido(10L, "Hamburguer", new BigDecimal("12.50"), 2)));
        pedido.setId(id);
        return pedido;
    }
}