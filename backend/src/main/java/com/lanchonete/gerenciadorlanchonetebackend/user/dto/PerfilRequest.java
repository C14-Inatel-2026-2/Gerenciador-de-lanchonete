package com.lanchonete.gerenciadorlanchonetebackend.user.dto;

/**
 * Corpo de POST /api/usuarios e PATCH /api/usuarios/me: só o nome.
 * E-mail e id vêm do token; uma "role" enviada no corpo é ignorada.
 */
public record PerfilRequest(String nome) {
}
