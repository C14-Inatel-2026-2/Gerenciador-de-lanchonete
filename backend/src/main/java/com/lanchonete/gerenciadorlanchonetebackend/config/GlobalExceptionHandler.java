package com.lanchonete.gerenciadorlanchonetebackend.config;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.CategoriaDuplicadaException;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.ProdutoDuplicadoException;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.TransicaoStatusInvalidaException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.EmailJaCadastradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Tratamento global de erros: toda resposta de erro da API tem o corpo
 * { "erro": "<mensagem>" }.
 *
 * Erros do próprio Spring MVC (JSON malformado, método não suportado, rota
 * inexistente...) são tratados pela classe base e convertidos para o mesmo formato.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERRO_INTERNO = "Erro interno do servidor";

    @ExceptionHandler({IllegalArgumentException.class, UserValidationException.class})
    public ResponseEntity<ErroResponse> handleRequisicaoInvalida(RuntimeException ex) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UsuarioNaoAutenticadoException.class)
    public ResponseEntity<ErroResponse> handleNaoAutenticado(UsuarioNaoAutenticadoException ex) {
        return erro(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> handleAcessoNegado(AcessoNegadoException ex) {
        return erro(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // EmailJaCadastradoException estende UserValidationException; o Spring escolhe
    // o handler mais específico, então ela cai aqui (409) e não no 400.
    @ExceptionHandler({
            EmailJaCadastradoException.class,
            CategoriaDuplicadaException.class,
            ProdutoDuplicadoException.class,
            TransicaoStatusInvalidaException.class,
            EstoqueInsuficienteException.class
    })
    public ResponseEntity<ErroResponse> handleConflito(RuntimeException ex) {
        return erro(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Qualquer erro não previsto: loga o stack trace e não expõe detalhes ao cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleErroInesperado(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, ERRO_INTERNO);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        String mensagem = statusCode.is4xxClientError() ? mensagemPadrao(statusCode) : ERRO_INTERNO;
        return ResponseEntity.status(statusCode).headers(headers).body(new ErroResponse(mensagem));
    }

    private static String mensagemPadrao(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "Requisição inválida";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método não permitido";
            case 415 -> "Tipo de conteúdo não suportado";
            default -> "Erro na requisição";
        };
    }

    private static ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(mensagem));
    }
}
