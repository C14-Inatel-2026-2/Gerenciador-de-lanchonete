package com.lanchonete.gerenciadorlanchonetebackend.user.dto;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;

/**
 * Corpo do PATCH /api/usuarios/{id}/role: { "role": "FUNCIONARIO" }.
 * Um valor fora do enum faz o Jackson falhar ao ler o JSON, e a API responde 400.
 */
public record AlterarRoleRequest(UserRole role) {
}
