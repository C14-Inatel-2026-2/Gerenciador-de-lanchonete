package com.lanchonete.gerenciadorlanchonetebackend.catalogo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CategoriaTest {

    @Test
    void deveCriarCategoriaAtivaPorPadrao(){
        Categoria categoria = new Categoria("Lanches");

        assertEquals("Lanches", categoria.getNome());
        assertTrue(categoria.isAtivo());
    }
}
