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

    const pedidosHoje = 24;
    const faturamento = 1250.50;
    const produtosAtivos = 32;
    const usuarios = 18;

    const pedidosRecentes = [
        {
            id: 1,
            cliente: "João",
            status: "Preparando"
        },
        {
            id: 2,
            cliente: "Maria",
            status: "Pronto"
        },
        {
            id: 3,
            cliente: "Carlos",
            status: "Entregue"
        }
    ];

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

            <TabelaPedidosRecentes pedidos={pedidosRecentes} />

        </main>
    );
}

export default AdminDashboard;