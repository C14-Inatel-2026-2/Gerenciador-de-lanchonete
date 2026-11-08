package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void deveSalvarCategoriaValida() {
        Categoria categoria = new Categoria("Lanches");
        when(categoriaRepository.existsByNome("Lanches")).thenReturn(false);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria salva = categoriaService.salvar(categoria);

        assertEquals(categoria, salva);
        verify(categoriaRepository).existsByNome("Lanches");
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void naoDeveSalvarCategoriaComNomeNulo() {
        Categoria categoria = new Categoria();

        assertThrows(IllegalArgumentException.class, () -> categoriaService.salvar(categoria));

        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void naoDeveSalvarCategoriaComNomeVazio() {
        Categoria categoria = new Categoria("");

        assertThrows(IllegalArgumentException.class, () -> categoriaService.salvar(categoria));

        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void naoDeveSalvarCategoriaComNomeApenasComEspacos() {
        Categoria categoria = new Categoria("   ");

        assertThrows(IllegalArgumentException.class, () -> categoriaService.salvar(categoria));

        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void naoDeveSalvarCategoriaComNomeDuplicado() {
        Categoria categoria = new Categoria("Lanches");
        when(categoriaRepository.existsByNome("Lanches")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoriaService.salvar(categoria));

        verify(categoriaRepository).existsByNome("Lanches");
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void deveListarCategorias() {
        List<Categoria> categorias = List.of(new Categoria("Lanches"), new Categoria("Bebidas"));
        when(categoriaRepository.findAll()).thenReturn(categorias);

        List<Categoria> resultado = categoriaService.listarCategorias();

        assertEquals(categorias, resultado);
        verify(categoriaRepository).findAll();
    }

    @Test
    void deveEditarCategoriaSemConsiderarSeuProprioNomeDuplicado() {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria editada = categoriaService.editar(1L, "Lanches");

        assertEquals("Lanches", editada.getNome());
        verify(categoriaRepository).existsByNomeAndIdNot("Lanches", 1L);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void naoDeveEditarCategoriaComNomeDuplicado() {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNomeAndIdNot("Bebidas", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoriaService.editar(1L, "Bebidas"));

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void naoDeveEditarCategoriaInexistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoriaService.editar(1L, "Lanches"));

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void deveDesativarCategoriaSemRemoveLa() {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria desativada = categoriaService.desativar(1L);

        assertFalse(desativada.isAtivo());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void naoDeveDesativarCategoriaInexistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoriaService.desativar(1L));

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }
}
