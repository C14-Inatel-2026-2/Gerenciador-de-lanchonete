import { ROTULO_STATUS } from "../utils/statusPedido";

function TabelaPedidosRecentes({ pedidos }) {
    return (
        <>
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
                    {pedidos.map((pedido) => (
                        <tr key={pedido.id}>
                            <td>{pedido.id}</td>
                            <td>{pedido.cliente}</td>
                            <td>{ROTULO_STATUS[pedido.status] ?? pedido.status}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
}

export default TabelaPedidosRecentes;