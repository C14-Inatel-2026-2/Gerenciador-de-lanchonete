import { describe, it, expect, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";

import Navbar from "../components/Navbar";
import { AuthProvider } from "../contexts/AuthContext";
import { CHAVE_SESSAO } from "../contexts/sessao";

function renderizar() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={["/cardapio"]}>
        <Routes>
          <Route path="/cardapio" element={<Navbar />} />
          <Route path="/login" element={<h1>Login</h1>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

describe("Navbar", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("mostra o nome de quem está logado", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify({ id: 1, nome: "Karolina", role: "CLIENTE" }));

    renderizar();

    expect(screen.getByText(/Olá, Karolina/)).toBeInTheDocument();
  });

  it("Sair encerra a sessão e volta para o login", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify({ id: 1, nome: "Karolina", role: "CLIENTE" }));
    renderizar();

    fireEvent.click(screen.getByRole("button", { name: "Sair" }));

    expect(screen.getByRole("heading", { name: "Login" })).toBeInTheDocument();
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });
});
