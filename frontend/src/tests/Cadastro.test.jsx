import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";

import Cadastro from "../pages/Cadastro";
import { AuthProvider } from "../contexts/AuthContext";
import { CHAVE_SESSAO } from "../contexts/sessao";
import { cadastrarConta } from "../services/autenticacao";

vi.mock("../services/autenticacao", () => ({ cadastrarConta: vi.fn() }));

function renderizar() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={["/cadastro"]}>
        <Routes>
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="/cardapio" element={<h1>Cardápio</h1>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

function preencher({ nome = "", email = "", senha = "" }) {
  fireEvent.change(screen.getByLabelText("Nome"), { target: { value: nome } });
  fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: email } });
  fireEvent.change(screen.getByLabelText("Senha"), { target: { value: senha } });
  fireEvent.click(screen.getByRole("button", { name: "Criar conta" }));
}

describe("Cadastro", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.resetAllMocks();
  });

  it("nome vazio bloqueia o envio", () => {
    renderizar();

    preencher({ nome: "  ", email: "k@email.com", senha: "123456" });

    expect(screen.getByRole("alert")).toHaveTextContent("Informe o nome.");
    expect(cadastrarConta).not.toHaveBeenCalled();
  });

  it("e-mail vazio bloqueia o envio", () => {
    renderizar();

    preencher({ nome: "Karolina", senha: "123456" });

    expect(screen.getByRole("alert")).toHaveTextContent("Informe o e-mail.");
    expect(cadastrarConta).not.toHaveBeenCalled();
  });

  it("senha com menos de 6 caracteres bloqueia o envio", () => {
    renderizar();

    preencher({ nome: "Karolina", email: "k@email.com", senha: "12345" });

    expect(screen.getByRole("alert")).toHaveTextContent("pelo menos 6 caracteres");
    expect(cadastrarConta).not.toHaveBeenCalled();
  });

  it("cadastro válido cria o CLIENTE e leva ao cardápio", async () => {
    const cliente = { id: 9, nome: "Karolina", email: "k@email.com", role: "CLIENTE" };
    cadastrarConta.mockResolvedValue(cliente);
    renderizar();

    preencher({ nome: " Karolina ", email: "k@email.com", senha: "123456" });

    expect(await screen.findByRole("heading", { name: "Cardápio" })).toBeInTheDocument();
    expect(cadastrarConta).toHaveBeenCalledWith({ nome: "Karolina", email: "k@email.com", senha: "123456" });
    expect(JSON.parse(localStorage.getItem(CHAVE_SESSAO))).toEqual(cliente);
  });

  it("e-mail duplicado mostra 'E-mail já cadastrado'", async () => {
    cadastrarConta.mockRejectedValue(new Error("E-mail já cadastrado"));
    renderizar();

    preencher({ nome: "Karolina", email: "admin@admin.com", senha: "123456" });

    expect(await screen.findByRole("alert")).toHaveTextContent("E-mail já cadastrado");
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });
});
