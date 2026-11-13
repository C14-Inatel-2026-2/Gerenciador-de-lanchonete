import { api } from "./api";
import { exigirSupabase } from "./supabase";

// Login e cadastro com o Supabase Auth (#42, US01).
// As funções devolvem o perfil da nossa API; quem chama faz entrar(perfil)
// no AuthContext. Os erros já vêm com a mensagem pronta para a tela.

// Códigos de erro do Supabase Auth (error.code)
const MENSAGENS_SUPABASE = {
  invalid_credentials: "E-mail ou senha incorretos.",
  weak_password: "A senha precisa ter pelo menos 6 caracteres.",
  user_already_exists: "E-mail já cadastrado",
  email_address_invalid: "E-mail inválido.",
  validation_failed: "E-mail inválido.",
  email_not_confirmed: "Confirme seu e-mail antes de entrar.",
  over_request_rate_limit: "Muitas tentativas. Aguarde um pouco e tente de novo.",
};

function erroDoSupabase(erro) {
  return new Error(
    MENSAGENS_SUPABASE[erro.code] ?? "Não foi possível falar com o serviço de login. Tente novamente."
  );
}

/** Apaga o token se a etapa na nossa API falhar, para não ficar meio logado. */
async function desfazerLogin(supabase) {
  await supabase.auth.signOut({ scope: "local" }).catch(() => {});
}

/** Login: senha no Supabase → perfil em GET /api/usuarios/me. */
export async function entrarComEmail({ email, senha }) {
  const supabase = exigirSupabase();
  const { error } = await supabase.auth.signInWithPassword({ email, password: senha });
  if (error) throw erroDoSupabase(error);

  try {
    // 401 "Perfil não encontrado" ou 403 "Usuário desativado" chegam aqui
    return await api("/api/usuarios/me");
  } catch (erro) {
    await desfazerLogin(supabase);
    throw erro;
  }
}

/**
 * Cadastro: conta no Supabase → perfil em POST /api/usuarios { nome }.
 * A role e o e-mail do perfil são decididos pelo backend a partir do token.
 *
 * Se a conta já existe no Supabase mas o perfil não (ex.: a API caiu no meio
 * do primeiro cadastro), entra com a mesma senha e só cria o perfil.
 * Se o perfil também já existe, a API responde 409 "E-mail já cadastrado".
 */
export async function cadastrarConta({ nome, email, senha }) {
  const supabase = exigirSupabase();
  const cadastro = await supabase.auth.signUp({ email, password: senha });

  if (cadastro.error?.code === "user_already_exists") {
    const login = await supabase.auth.signInWithPassword({ email, password: senha });
    if (login.error) throw erroDoSupabase(cadastro.error);
  } else if (cadastro.error) {
    throw erroDoSupabase(cadastro.error);
  } else if (!cadastro.data.session) {
    // Só acontece se o "Confirm email" for ligado no Supabase
    throw new Error("Enviamos um e-mail de confirmação. Confirme e depois entre.");
  }

  try {
    return await api("/api/usuarios", {
      method: "POST",
      body: JSON.stringify({ nome }),
    });
  } catch (erro) {
    await desfazerLogin(supabase);
    throw erro;
  }
}
