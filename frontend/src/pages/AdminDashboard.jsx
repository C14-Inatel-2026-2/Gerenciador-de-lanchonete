import { formatCurrency } from "../utils/formatCurrency";

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
                <div>
                    <h2>Pedidos hoje</h2>
                    <p>{pedidosHoje}</p>
                </div>

                <div>
                    <h2>Faturamento</h2>
                    <p>{formatCurrency(faturamento)}</p>
                </div>

                <div>
                    <h2>Produtos ativos</h2>
                    <p>{produtosAtivos}</p>
                </div>
                <div>
                    <h2>Usuários cadastrados</h2>
                    <p>{usuarios}</p>
                </div>
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