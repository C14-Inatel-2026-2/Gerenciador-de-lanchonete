package com.lanchonete.gerenciadorlanchonetebackend.config;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.TransicaoStatusInvalidaException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.EmailJaCadastradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ExcecaoTestController.class)
@Import(GlobalExceptionHandlerTest.ExcecaoTestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/teste")
    static class ExcecaoTestController {

        @GetMapping("/argumento-invalido")
        void argumentoInvalido() {
            throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
        }

        @GetMapping("/validacao-usuario")
        void validacaoUsuario() {
            throw new UserValidationException("Nome é obrigatório");
        }

        @GetMapping("/email-duplicado")
        void emailDuplicado() {
            throw new EmailJaCadastradoException();
        }

        @GetMapping("/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Produto não encontrado");
        }

        @GetMapping("/transicao-invalida")
        void transicaoInvalida() {
            throw new TransicaoStatusInvalidaException(StatusPedido.ENTREGUE, StatusPedido.CRIADO);
        }

        @GetMapping("/estoque-insuficiente")
        void estoqueInsuficiente() {
            throw new EstoqueInsuficienteException("Estoque insuficiente para X-Burguer");
        }

        @GetMapping("/acesso-negado")
        void acessoNegado() {
            throw new AcessoNegadoException("Acesso negado");
        }

        @GetMapping("/nao-autenticado")
        void naoAutenticado() {
            throw new UsuarioNaoAutenticadoException("Usuário não autenticado");
        }

        @GetMapping("/erro-inesperado")
        void erroInesperado() {
            throw new IllegalStateException("detalhe interno que não deve vazar");
        }

        @PostMapping("/corpo")
        void corpo(@RequestBody Map<String, Object> corpo) {
        }
    }

    @Test
    void illegalArgumentExceptionRetorna400() throws Exception {
        mockMvc.perform(get("/teste/argumento-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Preço do produto deve ser maior que zero"));
    }

    @Test
    void userValidationExceptionRetorna400() throws Exception {
        mockMvc.perform(get("/teste/validacao-usuario"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Nome é obrigatório"));
    }

    @Test
    void emailJaCadastradoRetorna409() throws Exception {
        mockMvc.perform(get("/teste/email-duplicado"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("E-mail já cadastrado"));
    }

    @Test
    void recursoNaoEncontradoRetorna404() throws Exception {
        mockMvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Produto não encontrado"));
    }

    @Test
    void transicaoStatusInvalidaRetorna409() throws Exception {
        mockMvc.perform(get("/teste/transicao-invalida"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Transição de status inválida: ENTREGUE -> CRIADO."));
    }

    @Test
    void estoqueInsuficienteRetorna409() throws Exception {
        mockMvc.perform(get("/teste/estoque-insuficiente"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Estoque insuficiente para X-Burguer"));
    }

    @Test
    void acessoNegadoRetorna403() throws Exception {
        mockMvc.perform(get("/teste/acesso-negado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    void usuarioNaoAutenticadoRetorna401() throws Exception {
        mockMvc.perform(get("/teste/nao-autenticado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));
    }

    @Test
    void erroInesperadoRetorna500SemExporDetalhes() throws Exception {
        mockMvc.perform(get("/teste/erro-inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.erro").value("Erro interno do servidor"));
    }

    @Test
    void jsonMalformadoRetorna400NoMesmoFormato() throws Exception {
        mockMvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON).content("{ invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição inválida"));
    }

    @Test
    void metodoNaoSuportadoRetorna405NoMesmoFormato() throws Exception {
        mockMvc.perform(post("/teste/nao-encontrado"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.erro").value("Método não permitido"));
    }
}
