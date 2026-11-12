import { useEffect, useState } from "react";
import { useAuth } from "../contexts/useAuth";
import { alterarPerfil, desativarUsuario, listarUsuarios } from "../services/usuarios";
import { PERFIS, rotuloDoPerfil } from "../utils/perfis";
import "./AdminUsuarios.css";

/** Compara ids sem depender do tipo (número da API x valor salvo na sessão). */
function mesmoId(a, b) {
  return a != null && b != null && String(a) === String(b);
}

export default function AdminUsuarios() {
  const { usuario: usuarioLogado } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erroLista, setErroLista] = useState("");
  const [erroAcao, setErroAcao] = useState("");
  const [aviso, setAviso] = useState("");
  // Uma ação por vez: enquanto uma alteração está sendo enviada, a tabela inteira
  // fica travada. Assim uma resposta não libera a linha de outra ação ainda pendente
  // e a mensagem na tela é sempre a da última ação.
  const [acaoEmAndamento, setAcaoEmAndamento] = useState(false);

  useEffect(() => {
    let ativo = true;

    listarUsuarios()
      .then((lista) => {
        if (ativo) setUsuarios(lista ?? []);
      })
      .catch((e) => {
        if (ativo) setErroLista(e.message);
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, []);

  async function executar(acao, mensagemDeSucesso) {
    setErroAcao("");
    setAviso("");
    setAcaoEmAndamento(true);
    try {
      const atualizado = await acao();
      if (!atualizado?.id) {
        throw new Error("O servidor não devolveu o usuário atualizado. Recarregue a página.");
      }
      setUsuarios((lista) => lista.map((u) => (u.id === atualizado.id ? atualizado : u)));
      setAviso(mensagemDeSucesso(atualizado));
    } catch (e) {
      setErroAcao(e.message);
    } finally {
      setAcaoEmAndamento(false);
    }
  }

  function mudarPerfil(usuario, role) {
    if (role === usuario.role) return;
    // Mudar perfil dá ou tira acesso (ADMIN gerencia tudo): confirma antes, inclusive
    // para não disparar com as setas do teclado no select
    const confirmou = window.confirm(
      `Alterar o perfil de ${usuario.nome} de ${rotuloDoPerfil(usuario.role)} para ${rotuloDoPerfil(role)}?`
    );
    if (!confirmou) return;
    executar(
      () => alterarPerfil(usuario.id, role),
      (atualizado) => `${atualizado.nome} agora é ${rotuloDoPerfil(atualizado.role)}.`
    );
  }

  function desativar(usuario) {
    const confirmou = window.confirm(
      `Desativar ${usuario.nome}? A conta continua no sistema, mas perde o acesso.`
    );
    if (!confirmou) return;
    executar(
      () => desativarUsuario(usuario.id),
      (atualizado) => `A conta de ${atualizado.nome} foi desativada.`
    );
  }

  return (
    <section className="usuarios-pagina">
      <header className="usuarios-cabecalho">
        <p className="usuarios-eyebrow">ADMINISTRAÇÃO</p>
        <h1>Usuários</h1>
        <p className="usuarios-descricao">
          Altere o perfil de quem trabalha na lanchonete e desative contas que não devem mais entrar.
        </p>
      </header>

      {erroAcao && <p className="usuarios-erro" role="alert">{erroAcao}</p>}
      {aviso && <p className="usuarios-aviso" role="status">{aviso}</p>}

      {carregando ? (
        <p>Carregando usuários...</p>
      ) : erroLista ? (
        <p className="usuarios-erro" role="alert">
          Não foi possível carregar os usuários: {erroLista}
        </p>
      ) : usuarios.length === 0 ? (
        <p>Nenhum usuário cadastrado.</p>
      ) : (
        <div className="usuarios-painel" aria-busy={acaoEmAndamento}>
          <table className="usuarios-tabela">
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">E-mail</th>
                <th scope="col">Perfil</th>
                <th scope="col">Situação</th>
                <th scope="col"><span className="usuarios-oculto">Ações</span></th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((usuario) => {
                const ehVoce = mesmoId(usuario.id, usuarioLogado?.id);
                // O backend também recusa (403); aqui só não oferece a ação
                const podeAlterar = usuario.ativo && !ehVoce;

                return (
                  <tr key={usuario.id} className={usuario.ativo ? "" : "usuarios-inativo"}>
                    <td>
                      {usuario.nome}
                      {ehVoce && <span className="usuarios-voce"> (você)</span>}
                    </td>
                    <td>{usuario.email}</td>
                    <td>
                      <select
                        aria-label={`Perfil de ${usuario.nome}`}
                        value={usuario.role}
                        disabled={!podeAlterar || acaoEmAndamento}
                        onChange={(e) => mudarPerfil(usuario, e.target.value)}
                      >
                        {PERFIS.map((perfil) => (
                          <option key={perfil.valor} value={perfil.valor}>
                            {perfil.rotulo}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <span className={usuario.ativo ? "usuarios-tag ativo" : "usuarios-tag"}>
                        {usuario.ativo ? "Ativo" : "Desativado"}
                      </span>
                    </td>
                    <td>
                      {podeAlterar && (
                        <button
                          type="button"
                          className="usuarios-botao-desativar"
                          disabled={acaoEmAndamento}
                          onClick={() => desativar(usuario)}
                        >
                          Desativar
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
