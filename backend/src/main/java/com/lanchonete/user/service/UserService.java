package com.lanchonete.user.service;

import com.lanchonete.user.exception.UserValidationException;
import com.lanchonete.user.model.UserProfile;
import com.lanchonete.user.model.UserRole;

public class UserService {

    public UserProfile criarPerfil(
            String supabaseUserId,
            String nome,
            String email
    ) {

        validarSupabaseUserId(supabaseUserId);
        validarNome(nome);
        validarEmail(email);

        return new UserProfile(
                supabaseUserId,
                nome,
                email,
                UserRole.CLIENTE
        );
    }

    private void validarSupabaseUserId(String supabaseUserId) {
        if (supabaseUserId == null || supabaseUserId.isBlank()) {
            throw new UserValidationException(
                    "Supabase User ID é obrigatório"
            );
        }
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new UserValidationException(
                    "Nome é obrigatório"
            );
        }
    }

    private void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UserValidationException(
                    "E-mail é obrigatório"
            );
        }

        if (!email.contains("@")) {
            throw new UserValidationException(
                    "E-mail inválido"
            );
        }
    }
}