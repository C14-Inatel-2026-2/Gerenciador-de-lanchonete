package com.lanchonete.gerenciadorlanchonetebackend.compras.service;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.CarrinhoCompras;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemCarrinho;
import org.springframework.stereotype.Service;

@Service
public class CarrinhoComprasService {

    public void adicionarItem(CarrinhoCompras carrinho, ItemCarrinho item) {
        if (carrinho == null) {
            throw new IllegalArgumentException("O carrinho não pode ser nulo.");
        }

        carrinho.adicionarItem(item);
    }
}