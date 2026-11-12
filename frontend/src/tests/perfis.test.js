import { describe, it, expect } from "vitest";
import { PERFIS, ROLES, rotuloDoPerfil } from "../utils/perfis";

describe("perfis", () => {
  it("tem os mesmos perfis do backend", () => {
    expect(ROLES).toEqual(["CLIENTE", "FUNCIONARIO", "ADMIN"]);
    expect(PERFIS).toHaveLength(ROLES.length);
  });

  it("rotuloDoPerfil traduz o perfil e devolve o próprio valor se for desconhecido", () => {
    expect(rotuloDoPerfil("FUNCIONARIO")).toBe("Funcionário");
    expect(rotuloDoPerfil("NOVO")).toBe("NOVO");
  });
});
