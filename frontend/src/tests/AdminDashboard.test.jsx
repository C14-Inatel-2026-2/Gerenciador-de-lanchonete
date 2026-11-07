import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import AdminDashboard from "../pages/AdminDashboard";

// Mock da função formatCurrency
vi.mock("../utils/formatCurrency", () => ({
    formatCurrency: vi.fn(() => "R$ TESTE"),
}));

describe("AdminDashboard", () => {

    it("deve exibir o título do painel", () => {

        render(<AdminDashboard />);

        expect(
            screen.getByText("Painel Administrativo")
        ).toBeInTheDocument();

    });

    it("deve exibir o valor retornado pelo mock da formatCurrency", () => {

        render(<AdminDashboard />);

        expect(
            screen.getByText("R$ TESTE")
        ).toBeInTheDocument();

    });

});