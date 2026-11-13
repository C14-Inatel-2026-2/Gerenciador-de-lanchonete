package com.lanchonete.gerenciadorlanchonetebackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Segurança da API (#55). Toda requisição passa por este filtro antes de chegar
 * ao controller: sem um token JWT válido do Supabase, a resposta é 401.
 *
 * Fechado por padrão: só as rotas listadas como públicas dispensam o token.
 * Quem é o usuário e o que ele pode fazer continua com o UsuarioAtualProvider
 * e o AuthorizationService.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    static final String NAO_AUTENTICADO = "Usuário não autenticado";
    static final String ACESSO_NEGADO = "Acesso negado";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint naoAutenticado =
                (request, response, ex) -> responder(response, HttpStatus.UNAUTHORIZED, NAO_AUTENTICADO);
        AccessDeniedHandler acessoNegado =
                (request, response, ex) -> responder(response, HttpStatus.FORBIDDEN, ACESSO_NEGADO);

        http
                // O token vai no header Authorization, não em cookie: não há CSRF a proteger.
                .csrf(AbstractHttpConfigurer::disable)
                // Usa as origens do CorsConfig (app.cors.origens) e responde o preflight antes da autenticação.
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        // Cardápio público (docs/api.md)
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/produtos", "/api/produtos/cardapio")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(jwt -> jwt
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(naoAutenticado)
                        .accessDeniedHandler(acessoNegado))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(naoAutenticado)
                        .accessDeniedHandler(acessoNegado));

        return http.build();
    }

    /** Mesmo corpo de erro do GlobalExceptionHandler: { "erro": "..." }. */
    private static void responder(HttpServletResponse response, HttpStatus status, String mensagem)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSON.writeValueAsString(new ErroResponse(mensagem)));
    }
}
