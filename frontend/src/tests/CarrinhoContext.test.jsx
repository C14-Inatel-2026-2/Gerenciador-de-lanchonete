import { act, renderHook, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { CarrinhoProvider } from "../contexts/CarrinhoContext";
import { useCarrinho } from "../contexts/useCarrinho";

const CHAVE_CARRINHO = "lanchonete.carrinho";

describe("CarrinhoContext", () => {
    beforeEach(() => {
        localStorage.removeItem(CHAVE_CARRINHO);
    });

    afterEach(() => {
        localStorage.removeItem(CHAVE_CARRINHO);
    });

    it("inicia vazio e persiste itens e total após alterações", async () => {
        const { result, unmount } = renderHook(() => useCarrinho(), {
            wrapper: CarrinhoProvider,
        });
        const xBurger = {
            id: 1,
            nome: "X-Burger",
            descricao: "Hambúrguer artesanal.",
            preco: 24.9,
        };
        const batatas = {
            id: 2,
            nome: "Batatas",
            descricao: "Porção de batatas.",
            preco: 12.5,
        };
        const produto = {
            id: 99,
            nome: "Produto de teste",
            descricao: "Descrição do produto",
            preco: 10,
        };

        expect(result.current.cart).toEqual([]);
        expect(result.current.total).toBe(0);

        act(() => {
            result.current.adicionarItem(xBurger);
            result.current.adicionarItem(batatas);
            result.current.adicionarItem(batatas);
            result.current.adicionarItem(produto);
            result.current.adicionarItem(produto);
            result.current.atualizarQuantidade(1, 3);
        });

        expect(result.current.cart.find((item) => item.id === 2).quantidade).toBe(2);
        expect(result.current.cart.find((item) => item.id === 99).quantidade).toBe(2);
        expect(result.current.cart.find((item) => item.id === 1).quantidade).toBe(3);
        expect(result.current.total).toBe(119.7);
        await waitFor(() => {
            expect(JSON.parse(localStorage.getItem(CHAVE_CARRINHO))).toEqual({
                itens: result.current.cart,
                total: result.current.total,
            });
        });

        const cartSalvo = JSON.parse(localStorage.getItem(CHAVE_CARRINHO));
        unmount();
        const restaurado = renderHook(() => useCarrinho(), {
            wrapper: CarrinhoProvider,
        });
        expect(restaurado.result.current.cart).toEqual(result.current.cart);
        expect(restaurado.result.current.total).toBe(cartSalvo.total);
        restaurado.unmount();
    });

    it("remove um item quando a quantidade é atualizada para zero", () => {
        const { result } = renderHook(() => useCarrinho(), {
            wrapper: CarrinhoProvider,
        });
        const produto = {
            id: 1,
            nome: "X-Burger",
            descricao: "Hambúrguer artesanal.",
            preco: 24.9,
        };

        act(() => {
            result.current.adicionarItem(produto);
            result.current.atualizarQuantidade(1, 0);
        });

        expect(result.current.cart.some((item) => item.id === 1)).toBe(false);
    });

    it("limpa os itens e zera o total", async () => {
        const { result } = renderHook(() => useCarrinho(), {
            wrapper: CarrinhoProvider,
        });

        act(() => result.current.limparCarrinho());

        expect(result.current.cart).toEqual([]);
        expect(result.current.total).toBe(0);
        await waitFor(() => {
            expect(JSON.parse(localStorage.getItem(CHAVE_CARRINHO))).toEqual({
                itens: [],
                total: 0,
            });
        });
    });
});
