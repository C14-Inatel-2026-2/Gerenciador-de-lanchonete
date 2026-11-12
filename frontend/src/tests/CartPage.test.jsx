import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import CartPage from "../pages/CartPage";

const cart = [
    {
        id: 1,
        nome: "X-Burger",
        descricao: "Hambúrguer com queijo.",
        preco: 24.9,
        quantidade: 2,
        quantidadeEstoque: 3,
    },
];

describe("CartPage", () => {
    it("exibe os itens, quantidades e total do carrinho", () => {
        render(
            <CartPage
                cart={cart}
                total={49.8}
                onFinishOrder={vi.fn()}
                onUpdateQuantity={vi.fn()}
            />
        );

        expect(screen.getByRole("heading", { name: "Meu carrinho" })).toBeInTheDocument();
        expect(screen.getByText("X-Burger")).toBeInTheDocument();
        expect(screen.getByText("2 itens selecionados")).toBeInTheDocument();
        expect(screen.getAllByText(/R\$\s?49,80/)).toHaveLength(3);
    });

    it("encaminha atualização de quantidade e finalização", () => {
        const onFinishOrder = vi.fn();
        const onUpdateQuantity = vi.fn();
        render(
            <CartPage
                cart={cart}
                total={49.8}
                onFinishOrder={onFinishOrder}
                onUpdateQuantity={onUpdateQuantity}
            />
        );

        fireEvent.click(screen.getByRole("button", { name: "Aumentar quantidade de X-Burger" }));
        fireEvent.click(screen.getByRole("button", { name: "Remover" }));
        fireEvent.click(screen.getByRole("button", { name: /Finalizar pedido/ }));

        expect(onUpdateQuantity).toHaveBeenNthCalledWith(1, 1, 3);
        expect(onUpdateQuantity).toHaveBeenNthCalledWith(2, 1, 0);
        expect(onFinishOrder).toHaveBeenCalledOnce();
    });

    it("mostra o estado vazio e desabilita a finalização", () => {
        render(
            <CartPage
                cart={[]}
                total={0}
                onFinishOrder={vi.fn()}
                onUpdateQuantity={vi.fn()}
            />
        );

        expect(screen.getByText("Seu carrinho está vazio")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Finalizar pedido/ })).toBeDisabled();
    });

    it("desabilita aumentar quantidade quando o estoque foi atingido", () => {
        render(
            <CartPage
                cart={[{ ...cart[0], quantidade: 3, quantidadeEstoque: 3 }]}
                total={74.7}
                onFinishOrder={vi.fn()}
                onUpdateQuantity={vi.fn()}
            />
        );

        expect(screen.getByRole("button", { name: "Aumentar quantidade de X-Burger" })).toBeDisabled();
    });
});
