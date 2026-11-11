import { formatCurrency } from "../utils/formatCurrency";
import CardMetrica from "../components/CardMetrica";

function AdminDashboard() {
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
            <h2>Pedidos Recentes</h2>

            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Cliente</th>
                        <th>Status</th>
                    </tr>
                </thead>

                <tbody>
                    {pedidosRecentes.map((pedido) => (
                        <tr key={pedido.id}>
                            <td>{pedido.id}</td>
                            <td>{pedido.cliente}</td>
                            <td>{pedido.status}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </main>
    );
}

export default AdminDashboard;