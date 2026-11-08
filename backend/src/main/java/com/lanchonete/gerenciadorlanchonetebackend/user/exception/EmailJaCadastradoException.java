package com.lanchonete.gerenciadorlanchonetebackend.user.exception;

/**
 * E-mail já usado por outro perfil. Estende UserValidationException para manter
 * compatibilidade, mas é respondida com HTTP 409 (conflito) em vez de 400.
 */
public class EmailJaCadastradoException extends UserValidationException {

    public EmailJaCadastradoException() {
        super("E-mail já cadastrado");
    }
}
