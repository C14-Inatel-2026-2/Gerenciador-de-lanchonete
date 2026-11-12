import { createClient } from "@supabase/supabase-js";

// Cliente do Supabase Auth (#42). A senha fica só no Supabase; a nossa API
// recebe o token (JWT) e guarda apenas o perfil (nome, e-mail, role).
//
// Sem as variáveis (ex.: nos testes e no build do Jenkins) o cliente fica null
// em vez de quebrar a aplicação ao carregar. Quem precisa dele usa exigirSupabase().
const url = import.meta.env.VITE_SUPABASE_URL;
const chave = import.meta.env.VITE_SUPABASE_ANON_KEY;

export const supabase = url && chave ? createClient(url, chave) : null;

export function exigirSupabase() {
  if (!supabase) {
    throw new Error(
      "Login indisponível: configure VITE_SUPABASE_URL e VITE_SUPABASE_ANON_KEY no .env.local"
    );
  }
  return supabase;
}
