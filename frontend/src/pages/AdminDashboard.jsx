import { useEffect, useState } from "react";
import { formatCurrency } from "../utils/formatCurrency";
import { api } from "../services/api";
import CardMetrica from "../components/CardMetrica";
import TabelaPedidosRecentes from "../components/TabelaPedidosRecentes";

function AdminDashboard() {
    const [carregando, setCarregando] = useState(false);
    const [erro, setErro] = useState("");
    const [resumo, setResumo] = useState(null);

    useEffect(() => {
        async function carregarResumo() {
            setCarregando(true);
            setErro("");

            try {
                const dados = await api("/api/admin/resumo");
                setResumo(dados);
            } catch (error) {
                setErro(error.message);
            } finally {
                setCarregando(false);
            }
        }

        carregarResumo();
    }, []);

    if (carregando) {
        return <p>Carregando painel...</p>;
    }

    if (erro) {
        return <p>Erro: {erro}</p>;
    }

    const pedidosHoje = resumo?.pedidosHoje ?? 0;
    const faturamento = resumo?.faturamentoHoje ?? 0;
    const produtosAtivos = resumo?.produtosAtivos ?? 0;
    const usuarios = resumo?.usuariosCadastrados ?? 0;
    const produtosComEstoqueBaixo = resumo?.produtosComEstoqueBaixo ?? [];

    const pedidosRecentes = resumo?.pedidosRecentes ?? [];

    return (
        <main>
            <h1>Painel Administrativo</h1>

            <section>
                <CardMetrica
                    titulo="Pedidos hoje"
                    valor={pedidosHoje}
                />

                <CardMetrica
                    titulo="Faturamento"
                    valor={formatCurrency(faturamento)}
                />

                <CardMetrica
                    titulo="Produtos ativos"
                    valor={produtosAtivos}
                />

                <CardMetrica
                    titulo="Usuários cadastrados"
                    valor={usuarios}
                />
            </section>

            {produtosComEstoqueBaixo.length > 0 && (
                <div className="alerta-estoque-baixo" role="alert">
                    <strong>⚠️ Estoque baixo:</strong>
                    <ul>
                        {produtosComEstoqueBaixo.map((produto) => (
                            <li key={produto.id}>
                                {`${produto.nome}: ${produto.quantidadeEstoque === 0 ? "esgotado" : `${produto.quantidadeEstoque} un.`}`}
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            <TabelaPedidosRecentes pedidos={pedidosRecentes} />
        </main>
    );
}

export default AdminDashboard;