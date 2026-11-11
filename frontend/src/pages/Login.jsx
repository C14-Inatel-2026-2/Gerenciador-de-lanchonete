import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";
import { entrarComEmail } from "../services/autenticacao";
import { paginaInicial } from "../utils/itensDoMenu";
import "./Auth.css";

export default function Login() {
  const { usuario, entrar } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  if (usuario) {
    return <Navigate to={paginaInicial(usuario.role)} replace />;
  }

  async function enviar(evento) {
    evento.preventDefault();

    if (!email.trim()) return setErro("Informe o e-mail.");
    if (!senha) return setErro("Informe a senha.");

    setErro("");
    setEnviando(true);
    try {
      const perfil = await entrarComEmail({ email: email.trim(), senha });
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
        <p className="auth-eyebrow">BOAS-VINDAS</p>
        <h1>Entrar</h1>
        <p className="auth-description">Acesse sua conta para fazer e acompanhar seus pedidos.</p>

        <form className="auth-form" onSubmit={enviar} noValidate>
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
              autoComplete="current-password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </label>

          {erro && <p className="auth-erro" role="alert">{erro}</p>}

          <button className="auth-botao" type="submit" disabled={enviando}>
            {enviando ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <p className="auth-rodape">
          Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link>
        </p>
      </div>
    </section>
  );
}
