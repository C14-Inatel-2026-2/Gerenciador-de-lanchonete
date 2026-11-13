package com.lanchonete.gerenciadorlanchonetebackend.admin.controller;

import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.PedidoRecenteResponse;
import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.ProdutoEstoqueBaixoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.admin.dto.ResumoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.admin.service.AdminService;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.config.SecurityConfig;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({AuthorizationService.class, SecurityConfig.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private UsuarioAtualProvider usuarioAtualProvider;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void adminRecebeOResumoNoFormatoDoContrato() throws Exception {
        logarComo(UserRole.ADMIN);
        when(adminService.obterResumo()).thenReturn(resumo());

        mockMvc.perform(get("/api/admin/resumo").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pedidosHoje").value(3))
            .andExpect(jsonPath("$.faturamentoHoje").value(50.0))
            .andExpect(jsonPath("$.ticketMedio").value(25.0))
            .andExpect(jsonPath("$.pedidosPorStatus.CRIADO").value(2))
            .andExpect(jsonPath("$.pedidosPorStatus.CANCELADO").value(1))
            .andExpect(jsonPath("$.produtosAtivos").value(32))
            .andExpect(jsonPath("$.usuariosCadastrados").value(18))
            .andExpect(jsonPath("$.pedidosRecentes[0].id").value(8))
            .andExpect(jsonPath("$.pedidosRecentes[0].cliente").value("Maria"))
            .andExpect(jsonPath("$.pedidosRecentes[0].status").value("PRONTO"))
            .andExpect(jsonPath("$.pedidosRecentes[0].total").value(25.0))
            .andExpect(jsonPath("$.pedidosRecentes[0].criadoEm").value("2026-11-12T19:30:00"))
            .andExpect(jsonPath("$.produtosComEstoqueBaixo[0].id").value(4))
            .andExpect(jsonPath("$.produtosComEstoqueBaixo[0].nome").value("Suco natural"))
            .andExpect(jsonPath("$.produtosComEstoqueBaixo[0].quantidadeEstoque").value(3));
    }

    @Test
    void clienteRecebe403SemCalcularOResumo() throws Exception {
        logarComo(UserRole.CLIENTE);

        mockMvc.perform(get("/api/admin/resumo").with(jwt()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.erro").value("Acesso restrito a administradores"));

        verifyNoInteractions(adminService);
    }

    @Test
    void funcionarioRecebe403SemCalcularOResumo() throws Exception {
        logarComo(UserRole.FUNCIONARIO);

        mockMvc.perform(get("/api/admin/resumo").with(jwt()))
            .andExpect(status().isForbidden());

        verifyNoInteractions(adminService);
    }

    @Test
    void adminDesativadoRecebe403() throws Exception {
        UserProfile desativado = perfil(UserRole.ADMIN);
        desativado.desativar();
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(desativado);

        mockMvc.perform(get("/api/admin/resumo").with(jwt()))
            .andExpect(status().isForbidden());

        verifyNoInteractions(adminService);
    }

    @Test
    void semTokenRetorna401SemChamarOService() throws Exception {
        mockMvc.perform(get("/api/admin/resumo"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(adminService, usuarioAtualProvider);
    }

    @Test
    void tokenValidoSemPerfilRetorna401() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual())
            .thenThrow(new UsuarioNaoAutenticadoException("Perfil não encontrado. Conclua o cadastro."));

        mockMvc.perform(get("/api/admin/resumo").with(jwt()))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminService);
    }

    private void logarComo(UserRole role) {
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(perfil(role));
    }

    private UserProfile perfil(UserRole role) {
        return new UserProfile("teste-" + role, "Usuário Teste", "teste@lanchonete.com", role);
    }

    private ResumoResponse resumo() {
        Map<StatusPedido, Long> porStatus = new EnumMap<>(StatusPedido.class);
        for (StatusPedido status : StatusPedido.values()) {
            porStatus.put(status, 0L);
        }
        porStatus.put(StatusPedido.CRIADO, 2L);
        porStatus.put(StatusPedido.CANCELADO, 1L);

        return new ResumoResponse(
                3, new BigDecimal("50.00"), new BigDecimal("25.00"), porStatus, 32, 18,
                List.of(new PedidoRecenteResponse(8L, "Maria", StatusPedido.PRONTO,
                        new BigDecimal("25.00"), LocalDateTime.of(2026, 11, 12, 19, 30))),
                List.of(new ProdutoEstoqueBaixoResponse(4L, "Suco natural", 3)));
    }
}
