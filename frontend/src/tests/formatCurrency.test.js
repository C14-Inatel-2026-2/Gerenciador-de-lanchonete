import { describe, it, expect } from "vitest";
import { formatCurrency } from "../utils/formatCurrency";

describe("formatCurrency", () => {

    it("deve formatar um valor para Real", () => {

        const resultado = formatCurrency(1250.5);

        expect(resultado).toContain("R$");
        expect(resultado).toContain("1.250,50");

    });

    it("deve lançar erro quando receber um valor inválido", () => {

        expect(() => formatCurrency(null)).toThrow();

    });

});