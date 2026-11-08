import { describe, it, expect } from "vitest";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { render, screen } from "@testing-library/react";

import ProtectedRoute from "../components/ProtectedRoute";
import { AuthContext } from "../contexts/useAuth";

describe("ProtectedRoute", () => {
  it("redireciona para login quando não existe usuário", () => {
    render(
      <AuthContext.Provider
        value={{
          usuario: null,
          estaLogado: false,
          entrar: () => {},
          sair: () => {},
          temPerfil: () => false,
        }}
      >
        <MemoryRouter initialEntries={["/cardapio"]}>
          <Routes>
            <Route path="/login" element={<h1>Login</h1>} />

            <Route
              path="/cardapio"
              element={
                <ProtectedRoute>
                  <h1>Cardápio</h1>
                </ProtectedRoute>
              }
            />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText("Login")).toBeInTheDocument();
  });

  it("permite acessar a página quando existe usuário", () => {
    render(
      <AuthContext.Provider
        value={{
          usuario: {
            id: 1,
            nome: "Yasmin",
            role: "ADMIN",
          },
          estaLogado: true,
          entrar: () => {},
          sair: () => {},
          temPerfil: () => true,
        }}
      >
        <MemoryRouter initialEntries={["/cardapio"]}>
          <Routes>
            <Route
              path="/cardapio"
              element={
                <ProtectedRoute>
                  <h1>Cardápio</h1>
                </ProtectedRoute>
              }
            />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    );

    expect(screen.getByText("Cardápio")).toBeInTheDocument();
  });
});