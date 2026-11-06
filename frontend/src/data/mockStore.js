export const mockProducts = [
    {
        id: 1,
        nome: "X-Burger da casa",
        descricao: "Hambúrguer artesanal, queijo, alface e molho especial.",
        preco: 24.9,
        disponivel: true,
        categoria: "Hambúrgueres",
    },
    {
        id: 2,
        nome: "Batata crocante",
        descricao: "Porção individual de batatas fritas.",
        preco: 12.5,
        disponivel: true,
        categoria: "Acompanhamentos",
    },
    {
        id: 3,
        nome: "Suco de laranja",
        descricao: "Suco natural, feito na hora.",
        preco: 8,
        disponivel: true,
        categoria: "Bebidas",
    },
    {
        id: 4,
        nome: "Cheeseburger",
        descricao: "Hambúrguer, queijo derretido e pão brioche.",
        preco: 21.5,
        disponivel: true,
        categoria: "Hambúrgueres",
    },
];

export const initialCart = [
    { ...mockProducts[0], quantidade: 1 },
    { ...mockProducts[1], quantidade: 2 },
];

const dateDaysAgo = (days, hour) => {
    const date = new Date();
    date.setDate(date.getDate() - days);
    date.setHours(hour, 20, 0, 0);
    return date.toISOString();
};

export const mockOrders = [
    {
        id: 1048,
        criadoEm: dateDaysAgo(0, 12),
        status: "EM_PREPARO",
        itens: [
            { produtoId: 1, nome: "X-Burger da casa", precoUnitario: 24.9, quantidade: 2 },
            { produtoId: 3, nome: "Suco de laranja", precoUnitario: 8, quantidade: 2 },
        ],
        endereco: "Av. Brasil, 450 — Jardim América",
        demonstrativo: true,
    },
    {
        id: 1047,
        criadoEm: dateDaysAgo(0, 11),
        status: "PRONTO",
        itens: [
            { produtoId: 2, nome: "Batata crocante", precoUnitario: 12.5, quantidade: 1 },
            { produtoId: 4, nome: "Cheeseburger", precoUnitario: 21.5, quantidade: 1 },
        ],
        endereco: "Rua do Comércio, 82 — Centro",
        demonstrativo: true,
    },
    {
        id: 1046,
        criadoEm: dateDaysAgo(1, 19),
        status: "ENTREGUE",
        itens: [
            { produtoId: 4, nome: "Cheeseburger", precoUnitario: 21.5, quantidade: 2 },
        ],
        endereco: "Rua Ipê Amarelo, 17 — Vila Nova",
        demonstrativo: true,
    },
];
