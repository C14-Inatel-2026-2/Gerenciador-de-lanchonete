import { formatCurrency } from "../utils/formatCurrency";

function AdminDashboard() {
    const pedidosHoje = 24;
    const faturamento = 1250.50;
    const produtosAtivos = 32;

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
            </section>
        </main>
    );
}

export default AdminDashboard;