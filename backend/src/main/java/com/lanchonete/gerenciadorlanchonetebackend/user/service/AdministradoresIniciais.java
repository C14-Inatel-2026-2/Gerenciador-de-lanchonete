package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Primeiro administrador do sistema (#55).
 *
 * Todo cadastro novo nasce CLIENTE, então sem esta regra ninguém conseguiria
 * entrar no painel admin. Os e-mails da variável ADMIN_EMAILS (separados por
 * vírgula) nascem ADMIN. Depois, esse admin promove os outros pela tela
 * /admin/usuarios.
 */
@Component
public class AdministradoresIniciais {

    private final Set<String> emails;

    public AdministradoresIniciais(@Value("${app.admin.emails:}") String emails) {
        this.emails = Arrays.stream(emails.split(","))
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Sem nenhum administrador inicial (usado nos testes). */
    public static AdministradoresIniciais nenhum() {
        return new AdministradoresIniciais("");
    }

    /** @param emailNormalizado e-mail já em minúsculas e sem espaços */
    public boolean contem(String emailNormalizado) {
        return emails.contains(emailNormalizado);
    }
}
