package com.lanchonete.gerenciadorlanchonetebackend.user.model;

import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import jakarta.persistence.*;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String supabaseUserId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean ativo;

    protected UserProfile() {
    }

    public UserProfile(
            String supabaseUserId,
            String nome,
            String email,
            UserRole role
    ) {
        this.supabaseUserId = supabaseUserId;
        this.nome = nome;
        this.email = email;
        if (role == null) {
            throw new UserValidationException("Role é obrigatória");
        }
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

    public void desativar() {
        this.ativo = false;
    }
}