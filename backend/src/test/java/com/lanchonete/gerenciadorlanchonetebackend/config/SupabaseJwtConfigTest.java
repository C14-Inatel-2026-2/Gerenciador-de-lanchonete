package com.lanchonete.gerenciadorlanchonetebackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regras que um token do Supabase precisa cumprir além da assinatura (#55). */
class SupabaseJwtConfigTest {

    private static final String EMISSOR = "https://projeto.supabase.co/auth/v1";

    private final OAuth2TokenValidator<Jwt> validador = SupabaseJwtConfig.validador(EMISSOR);

    private static Jwt.Builder token() {
        Instant agora = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "ES256")
                .subject("sub-123")
                .issuer(EMISSOR)
                .audience(List.of("authenticated"))
                .issuedAt(agora.minusSeconds(60))
                .expiresAt(agora.plusSeconds(3600));
    }

    @Test
    void aceitaTokenDoProjetoDentroDaValidade() {
        assertFalse(validador.validate(token().build()).hasErrors());
    }

    @Test
    void recusaTokenExpirado() {
        Jwt expirado = token()
                .issuedAt(Instant.now().minusSeconds(7200))
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();

        assertTrue(validador.validate(expirado).hasErrors());
    }

    @Test
    void recusaTokenDeOutroProjetoDoSupabase() {
        Jwt outroProjeto = token().issuer("https://outro.supabase.co/auth/v1").build();

        assertTrue(validador.validate(outroProjeto).hasErrors());
    }

    @Test
    void recusaTokenQueNaoEDeUsuarioLogado() {
        // Ex.: token da chave anon/service, que não tem a audiência "authenticated"
        Jwt semAudiencia = token().audience(List.of("anon")).build();

        assertTrue(validador.validate(semAudiencia).hasErrors());
    }

    @Test
    void emissorEMontadoAPartirDaUrlDoProjeto() {
        assertEquals(EMISSOR, SupabaseJwtConfig.emissor("https://projeto.supabase.co/"));
    }

    @Test
    void semSupabaseUrlAAplicacaoNaoSobe() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new SupabaseJwtConfig().jwtDecoder(" "));
        assertTrue(ex.getMessage().contains("SUPABASE_URL"));
    }
}
