package com.lanchonete.gerenciadorlanchonetebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * Valida os tokens emitidos pelo Supabase Auth (#55).
 *
 * O projeto assina os tokens com chave assimétrica (ES256): o backend baixa a
 * chave PÚBLICA do endereço JWKS do Supabase, então nenhum segredo fica na
 * aplicação. Só a URL do projeto é configurada (SUPABASE_URL).
 */
@Configuration
public class SupabaseJwtConfig {

    /** Todo usuário logado no Supabase recebe esta audiência no token. */
    static final String AUDIENCIA = "authenticated";

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.supabase.url:}") String supabaseUrl) {
        if (!StringUtils.hasText(supabaseUrl)) {
            throw new IllegalStateException(
                    "Defina a variável SUPABASE_URL (ex.: https://<projeto>.supabase.co)");
        }
        String emissor = emissor(supabaseUrl);

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(emissor + "/.well-known/jwks.json")
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(validador(emissor));
        return decoder;
    }

    /** https://<projeto>.supabase.co → https://<projeto>.supabase.co/auth/v1 */
    static String emissor(String supabaseUrl) {
        return supabaseUrl.trim().replaceAll("/+$", "") + "/auth/v1";
    }

    /** Expiração, emissor (o nosso projeto do Supabase) e audiência de usuário logado. */
    static OAuth2TokenValidator<Jwt> validador(String emissor) {
        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(emissor),
                new JwtClaimValidator<Collection<String>>(
                        JwtClaimNames.AUD, audiencia -> audiencia != null && audiencia.contains(AUDIENCIA))
        );
    }
}
