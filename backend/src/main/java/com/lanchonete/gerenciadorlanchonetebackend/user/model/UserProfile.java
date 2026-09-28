package com.lanchonete.gerenciadorlanchonetebackend.user.model;

public class UserProfile {

    private Long id;
    private String supabaseUserId;
    private String nome;
    private String email;
    private UserRole role;
    private boolean ativo;

    public UserProfile(
            String supabaseUserId,
            String nome,
            String email,
            UserRole role
    ) {
        this.supabaseUserId = supabaseUserId;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public String getSupabaseUserId() {
        return supabaseUserId;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAtivo() {
        return ativo;
    }
}