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