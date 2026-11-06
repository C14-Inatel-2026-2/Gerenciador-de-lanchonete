import { createContext, useContext } from "react";

// Fica fora do AuthContext.jsx para o arquivo .jsx exportar só componentes
// (regra react/only-export-components do oxlint).
export const AuthContext = createContext(null);

/**
 * Acesso ao usuário logado nos componentes:
 *   const { usuario, estaLogado, entrar, sair, temPerfil } = useAuth();
 */
export function useAuth() {
  const contexto = useContext(AuthContext);
  if (contexto === null) {
    throw new Error("useAuth precisa ser usado dentro de <AuthProvider>");
  }
  return contexto;
}
