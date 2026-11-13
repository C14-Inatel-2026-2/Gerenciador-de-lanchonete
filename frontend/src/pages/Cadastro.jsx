import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";
import { cadastrarConta } from "../services/autenticacao";
import { paginaInicial } from "../utils/itensDoMenu";
import "./Auth.css";

// Mesmo mínimo configurado no Supabase (Authentication > Password length)
const TAMANHO_MINIMO_SENHA = 6;

function validar({ nome, email, senha }) {
  if (!nome.trim()) return "Informe o nome.";
  if (!email.trim()) return "Informe o e-mail.";
  if (senha.length < TAMANHO_MINIMO_SENHA) {
    return `A senha precisa ter pelo menos ${TAMANHO_MINIMO_SENHA} caracteres.`;
  }
  return "";
}

export default function Cadastro() {
  const { usuario, entrar } = useAuth();
  const navigate = useNavigate();
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  if (usuario) {
    return <Navigate to={paginaInicial(usuario.role)} replace />;
  }

  async function enviar(evento) {
    evento.preventDefault();

    const problema = validar({ nome, email, senha });
    if (problema) return setErro(problema);

    setErro("");
    setEnviando(true);
    try {
      const perfil = await cadastrarConta({ nome: nome.trim(), email: email.trim(), senha });
      const salvo = entrar(perfil);
      navigate(paginaInicial(salvo.role), { replace: true });
    } catch (e) {
      setErro(e.message);
      setEnviando(false);
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="auth-eyebrow">PRIMEIRO ACESSO</p>
        <h1>Criar conta</h1>
        <p className="auth-description">Cadastre-se para montar seu pedido pelo cardápio.</p>

        <form className="auth-form" onSubmit={enviar} noValidate>
          <label>
            Nome
            <input
              autoComplete="name"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
            />
          </label>

          <label>
            E-mail
            <input
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>

          <label>
            Senha
            <input
              type="password"
              autoComplete="new-password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </label>

          {erro && <p className="auth-erro" role="alert">{erro}</p>}

          <button className="auth-botao" type="submit" disabled={enviando}>
            {enviando ? "Criando conta..." : "Criar conta"}
          </button>
        </form>

        <p className="auth-rodape">
          Já tem conta? <Link to="/login">Entrar</Link>
        </p>
      </div>
    </section>
  );
}
