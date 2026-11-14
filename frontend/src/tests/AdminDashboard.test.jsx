import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import AdminDashboard from "../pages/AdminDashboard";
import { api } from "../services/api";

// Mock da API
vi.mock("../services/api", () => ({
    api: vi.fn(async () => ({
        pedidosHoje: 12,
        faturamentoHoje: 344.4,
        produtosAtivos: 32,
        usuariosCadastrados: 18,
        pedidosRecentes: [],
    })),
}));

// Mock da função formatCurrency
vi.mock("../utils/formatCurrency", () => ({
    formatCurrency: vi.fn(() => "R$ TESTE"),
}));

describe("AdminDashboard", () => {

    it("deve listar os produtos com estoque baixo", async () => {
    api.mockResolvedValueOnce({
        pedidosHoje: 0,
        faturamentoHoje: 0,
        produtosAtivos: 2,
        usuariosCadastrados: 1,
        pedidosRecentes: [],
        produtosComEstoqueBaixo: [
            { id: 7, nome: "Refrigerante", quantidadeEstoque: 0 },
            { id: 3, nome: "Suco natural", quantidadeEstoque: 3 },
        ],
    });

    render(<AdminDashboard />);

    expect(await screen.findByText("Refrigerante: esgotado")).toBeInTheDocument();
    expect(screen.getByText("Suco natural: 3 un.")).toBeInTheDocument();
});
    it("deve exibir o título do painel", async () => {
        render(<AdminDashboard />);

        expect(
            await screen.findByText("Painel Administrativo")
        ).toBeInTheDocument();
    });

    it("deve exibir o valor retornado pelo mock da formatCurrency", async () => {
        render(<AdminDashboard />);

        expect(
            await screen.findByText("R$ TESTE")
        ).toBeInTheDocument();
    });
});