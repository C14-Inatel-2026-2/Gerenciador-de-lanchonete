import { describe, it, expect } from "vitest";
import { itensDoMenu } from "../utils/itensDoMenu";

describe("itensDoMenu", () => {

    it("deve retornar os itens do CLIENTE", () => {

        const itens = itensDoMenu("CLIENTE");

        expect(itens).toHaveLength(3);
        expect(itens[0].label).toBe("Cardápio");
        expect(itens[1].label).toBe("Carrinho");
        expect(itens[2].label).toBe("Meus pedidos");

    });

    it("deve retornar os itens do FUNCIONARIO", () => {

        const itens = itensDoMenu("FUNCIONARIO");

        expect(itens).toHaveLength(1);
        expect(itens[0].label).toBe("Cozinha");

    });

    it("deve retornar todos os itens do ADMIN", () => {

        const itens = itensDoMenu("ADMIN");

        expect(itens).toHaveLength(7);
        expect(itens[6].label).toBe("Usuários");

    });

    it("deve retornar lista vazia para perfil inválido", () => {

        expect(itensDoMenu("TESTE")).toEqual([]);

    });

});