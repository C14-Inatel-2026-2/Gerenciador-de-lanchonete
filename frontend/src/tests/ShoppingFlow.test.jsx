import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "../App";

describe("fluxo de carrinho e pedidos", () => {
    it("atualiza quantidade e total do carrinho", () => {
        render(<App />);

        fireEvent.click(screen.getByRole("button", { name: "Aumentar quantidade de X-Burger da casa" }));

        expect(screen.getByText(/R\$\s?49,80/)).toBeInTheDocument();
        expect(screen.getAllByText(/R\$\s?74,80/)).toHaveLength(2);
    });

    it("expande os detalhes do pedido selecionado", () => {
        render(<App />);
        fireEvent.click(screen.getByRole("button", { name: "Pedidos" }));
        fireEvent.click(screen.getByRole("button", { name: /Pedido #1048/ }));

        expect(screen.getByText("Av. Brasil, 450 — Jardim América")).toBeInTheDocument();
        expect(screen.getByText(/X-Burger da casa/)).toBeInTheDocument();
        expect(screen.getAllByText("Em preparo")).toHaveLength(2);
    });

    it("cria um pedido demonstrativo e limpa o carrinho após finalizar", () => {
        render(<App />);
        fireEvent.click(screen.getByRole("button", { name: /Finalizar pedido/ }));

        expect(screen.getByText("Pedido #1049")).toBeInTheDocument();
        expect(screen.getByText(/Pedido #1049 criado no modo demonstrativo/)).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "Carrinho" }));
        expect(screen.getByText("Seu carrinho está vazio")).toBeInTheDocument();
    });
});
