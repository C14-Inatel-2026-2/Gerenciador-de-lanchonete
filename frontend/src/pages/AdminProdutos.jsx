import { useCallback, useEffect, useState } from "react";
import { api } from "../services/api";
import { formatCurrency } from "../utils/formatCurrency";

const FORMULARIO_VAZIO = {
  nome: "",
  descricao: "",
  preco: "",
  categoriaId: "",
  quantidadeEstoque: "0",
};

export default function AdminProdutos() {
  const [produtos, setProdutos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [formulario, setFormulario] = useState(FORMULARIO_VAZIO);
  const [produtoEmEdicao, setProdutoEmEdicao] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");
  const [mensagem, setMensagem] = useState("");

  const carregarDados = useCallback(async () => {
    setErro("");
    const [produtosRecebidos, categoriasRecebidas] = await Promise.all([
      api("/api/produtos"),
      api("/api/categorias"),
    ]);
    setProdutos(produtosRecebidos ?? []);
    setCategorias(categoriasRecebidas ?? []);
  }, []);

  useEffect(() => {
    let ativo = true;

    async function carregar() {
      try {
        const [produtosRecebidos, categoriasRecebidas] = await Promise.all([
          api("/api/produtos"),
          api("/api/categorias"),
        ]);
        if (ativo) {
          setProdutos(produtosRecebidos ?? []);
          setCategorias(categoriasRecebidas ?? []);
        }
      } catch (erroRequisicao) {
        if (ativo) {
          setErro(erroRequisicao.message);
        }
      } finally {
        if (ativo) {
          setCarregando(false);
        }
      }
    }

    carregar();
    return () => {
      ativo = false;
    };
  }, []);

  function atualizarCampo(evento) {
    const { name, value } = evento.target;
    setFormulario((atual) => ({ ...atual, [name]: value }));
  }

  function iniciarEdicao(produto) {
    setProdutoEmEdicao(produto.id);
    setFormulario({
      nome: produto.nome,
      descricao: produto.descricao ?? "",
      preco: String(produto.preco),
      categoriaId: String(produto.categoriaId),
      quantidadeEstoque: String(produto.quantidadeEstoque ?? 0),
    });
    setMensagem("");
    setErro("");
  }

  function cancelarEdicao() {
    setProdutoEmEdicao(null);
    setFormulario(FORMULARIO_VAZIO);
    setErro("");
    setMensagem("");
  }

  async function salvarProduto(evento) {
    evento.preventDefault();
    setErro("");
    setMensagem("");
    setSalvando(true);

    const dadosProduto = {
      nome: formulario.nome.trim(),
      descricao: formulario.descricao.trim(),
      preco: Number(formulario.preco),
      categoriaId: Number(formulario.categoriaId),
      quantidadeEstoque: Number(formulario.quantidadeEstoque),
    };

    try {
      if (produtoEmEdicao === null) {
        await api("/api/produtos", {
          method: "POST",
          body: JSON.stringify(dadosProduto),
        });
        setMensagem("Produto cadastrado com sucesso.");
      } else {
        await api(`/api/produtos/${produtoEmEdicao}`, {
          method: "PUT",
          body: JSON.stringify(dadosProduto),
        });
        setMensagem("Produto atualizado com sucesso.");
      }

      await carregarDados();
      setProdutoEmEdicao(null);
      setFormulario(FORMULARIO_VAZIO);
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    } finally {
      setSalvando(false);
    }
  }

  async function desativarProduto(produto) {
    setErro("");
    setMensagem("");
    try {
      await api(`/api/produtos/${produto.id}/desativar`, { method: "PATCH" });
      setProdutos((atuais) => atuais.filter((item) => item.id !== produto.id));
      if (produtoEmEdicao === produto.id) {
        cancelarEdicao();
      }
      setMensagem(`Produto "${produto.nome}" desativado.`);
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    }
  }

  async function ajustarEstoque(produto) {
    setErro("");
    setMensagem("");
    if (String(produto.quantidadeEstoque).trim() === "") {
      setErro("Estoque deve ser um número inteiro igual ou maior que zero.");
      return;
    }
    const quantidade = Number(produto.quantidadeEstoque);
    if (!Number.isInteger(quantidade) || quantidade < 0) {
      setErro("Estoque deve ser um número inteiro igual ou maior que zero.");
      return;
    }

    try {
      const resposta = await api(`/api/produtos/${produto.id}/estoque`, {
        method: "PATCH",
        body: JSON.stringify({ quantidade }),
      });
      setProdutos((atuais) =>
        atuais.map((item) =>
          item.id === produto.id
            ? {
                ...item,
                quantidadeEstoque: resposta?.quantidadeEstoque ?? quantidade,
                esgotado: resposta?.esgotado ?? quantidade === 0,
              }
            : item
        )
      );
      setMensagem(`Estoque de "${produto.nome}" atualizado.`);
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    }
  }

  if (carregando) {
    return (
      <section className="admin-produtos" aria-labelledby="titulo-admin-produtos">
        <h1 id="titulo-admin-produtos">Gerenciar produtos</h1>
        <p role="status">Carregando produtos...</p>
      </section>
    );
  }

  return (
    <section className="admin-produtos" aria-labelledby="titulo-admin-produtos">
      <h1 id="titulo-admin-produtos">Gerenciar produtos</h1>

      <div className="admin-produtos__formulario">
        <h2>{produtoEmEdicao === null ? "Cadastrar produto" : "Editar produto"}</h2>
        <form onSubmit={salvarProduto} className="admin-produtos__campos">
          <div className="admin-produtos__campo">
            <label htmlFor="produto-nome">Nome</label>
            <input
              id="produto-nome"
              name="nome"
              value={formulario.nome}
              onChange={atualizarCampo}
              required
            />
          </div>
          <div className="admin-produtos__campo admin-produtos__campo--descricao">
            <label htmlFor="produto-descricao">Descrição</label>
            <textarea
              id="produto-descricao"
              name="descricao"
              value={formulario.descricao}
              onChange={atualizarCampo}
            />
          </div>
          <div className="admin-produtos__campo">
            <label htmlFor="produto-preco">Preço</label>
            <input
              id="produto-preco"
              name="preco"
              type="number"
              min="0.01"
              step="0.01"
              value={formulario.preco}
              onChange={atualizarCampo}
              required
            />
          </div>
          <div className="admin-produtos__campo">
            <label htmlFor="produto-categoria">Categoria</label>
            <select
              id="produto-categoria"
              name="categoriaId"
              value={formulario.categoriaId}
              onChange={atualizarCampo}
              required
            >
              <option value="">Selecione uma categoria</option>
              {categorias.map((categoria) => (
                <option key={categoria.id} value={String(categoria.id)}>
                  {categoria.nome}
                </option>
              ))}
            </select>
          </div>
          <div className="admin-produtos__campo">
            <label htmlFor="produto-estoque">Estoque</label>
            <input
              id="produto-estoque"
              name="quantidadeEstoque"
              type="number"
              min="0"
              step="1"
              value={formulario.quantidadeEstoque}
              onChange={atualizarCampo}
              required
            />
          </div>
          <div className="admin-produtos__acoes">
            <button type="submit" disabled={salvando || categorias.length === 0}>
              {salvando
                ? "Salvando..."
                : produtoEmEdicao === null
                  ? "Cadastrar produto"
                  : "Salvar alterações"}
            </button>
            {produtoEmEdicao !== null && (
              <button type="button" onClick={cancelarEdicao} disabled={salvando}>
                Cancelar edição
              </button>
            )}
          </div>
        </form>
      </div>

      {categorias.length === 0 && (
        <p role="status">
          Cadastre uma categoria antes de cadastrar produtos.
        </p>
      )}
      {erro && <p role="alert">{erro}</p>}
      {mensagem && <p role="status">{mensagem}</p>}

      <h2 className="admin-produtos__lista-titulo">Produtos disponíveis</h2>
      {produtos.length === 0 ? (
        <p role="status">Nenhum produto disponível cadastrado.</p>
      ) : (
        <ul className="admin-produtos__lista">
          {produtos.map((produto) => {
            const categoria = categorias.find(
              (item) => String(item.id) === String(produto.categoriaId)
            );
            return (
              <li className="admin-produtos__item" key={produto.id}>
                <h3>{produto.nome}</h3>
                {produto.descricao && <p>{produto.descricao}</p>}
                <p>{formatCurrency(produto.preco)}</p>
                <p>Categoria: {categoria?.nome ?? "Categoria indisponível"}</p>
                <p>Estoque: {produto.quantidadeEstoque ?? 0}</p>
                <label htmlFor={`estoque-${produto.id}`}>Ajustar estoque</label>
                <input
                  id={`estoque-${produto.id}`}
                  type="number"
                  min="0"
                  step="1"
                  value={produto.quantidadeEstoque ?? 0}
                  onChange={(evento) => {
                    const quantidadeEstoque = evento.target.value;
                    setProdutos((atuais) =>
                      atuais.map((item) =>
                        item.id === produto.id
                          ? { ...item, quantidadeEstoque }
                          : item
                      )
                    );
                  }}
                />
                <button type="button" onClick={() => ajustarEstoque(produto)}>
                  Salvar estoque
                </button>
                <div className="admin-produtos__item-acoes">
                  <button type="button" onClick={() => iniciarEdicao(produto)}>
                    Editar
                  </button>
                  <button type="button" onClick={() => desativarProduto(produto)}>
                    Desativar
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
