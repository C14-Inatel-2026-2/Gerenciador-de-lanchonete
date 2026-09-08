package com.lanchonete.user.service;

import com.lanchonete.user.model.UserProfile;
import com.lanchonete.user.model.UserRole;

public class UserService {

    public UserProfile criarPerfil(
            String supabaseUserId,
            String nome,
            String email
    ) {
        return new UserProfile(
                supabaseUserId,
                nome,
                email,
                UserRole.CLIENTE
        );
    }
}