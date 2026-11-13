import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import AdminDashboard from "../pages/AdminDashboard";

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