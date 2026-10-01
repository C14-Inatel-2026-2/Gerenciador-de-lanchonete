package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile criarPerfil(
            String supabaseUserId,
            String nome,
            String email
    ) {

        validarSupabaseUserId(supabaseUserId);
        validarNome(nome);
        validarEmail(email);

        if (userProfileRepository.existsByEmail(email)) {
            throw new UserValidationException("E-mail já cadastrado");
        }

        UserProfile userProfile = new UserProfile(
                supabaseUserId,
                nome,
                email,
                UserRole.CLIENTE
        );
        return userProfileRepository.save(userProfile);
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

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new UserValidationException(
                    "E-mail inválido"
            );
        }
    }
}
