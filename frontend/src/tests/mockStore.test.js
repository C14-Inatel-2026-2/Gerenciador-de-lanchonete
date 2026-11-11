import { describe, expect, it } from "vitest";
import { initialCart, mockOrders, mockProducts } from "../data/mockStore";

describe("mockProducts", () => {
    it("segue o formato CardapioResponse documentado", () => {
        expect(mockProducts.mensagem).toBeNull();
        expect(mockProducts.categorias.length).toBeGreaterThan(0);

        for (const categoria of mockProducts.categorias) {
            expect(Object.keys(categoria).sort()).toEqual(["id", "nome", "produtos"]);
            expect(categoria.produtos.length).toBeGreaterThan(0);

            for (const produto of categoria.produtos) {
                expect(Object.keys(produto).sort()).toEqual(["descricao", "id", "nome", "preco"]);
            }
        }
    });

    it("deriva o carrinho demonstrativo de produtos do mock do cardápio", () => {
        expect(initialCart.map(({ id, quantidade }) => ({ id, quantidade }))).toEqual([
            { id: 1, quantidade: 1 },
            { id: 2, quantidade: 2 },
        ]);
        expect(initialCart.every((produto) => !("categoria" in produto))).toBe(true);
    });
});

describe("mockOrders", () => {
    it("segue o formato PedidoResponse documentado", () => {
        expect(mockOrders.length).toBeGreaterThan(0);

        for (const pedido of mockOrders) {
            expect(Object.keys(pedido).sort()).toEqual([
                "clienteId",
                "criadoEm",
                "id",
                "itens",
                "status",
                "total",
            ]);
            expect(pedido.itens.length).toBeGreaterThan(0);

            for (const item of pedido.itens) {
                expect(Object.keys(item).sort()).toEqual([
                    "nome",
                    "precoUnitario",
                    "produtoId",
                    "quantidade",
                    "subtotal",
                ]);
            }
        }
    });
});
