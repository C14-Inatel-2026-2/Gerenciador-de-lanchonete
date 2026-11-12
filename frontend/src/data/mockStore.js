export const mockProducts = {
    categorias: [
        {
            id: 1,
            nome: "Lanches",
            produtos: [
                {
                    id: 1,
                    nome: "X-Burger da casa",
                    descricao: "Hambúrguer artesanal, queijo, alface e molho especial.",
                    preco: 24.9,
                },
                {
                    id: 4,
                    nome: "Cheeseburger",
                    descricao: "Hambúrguer, queijo derretido e pão brioche.",
                    preco: 21.5,
                },
            ],
        },
        {
            id: 2,
            nome: "Acompanhamentos",
            produtos: [
                {
                    id: 2,
                    nome: "Batata crocante",
                    descricao: "Porção individual de batatas fritas.",
                    preco: 12.5,
                },
            ],
        },
        {
            id: 3,
            nome: "Bebidas",
            produtos: [
                {
                    id: 3,
                    nome: "Suco de laranja",
                    descricao: "Suco natural, feito na hora.",
                    preco: 8,
                },
            ],
        },
    ],
    mensagem: null,
};

const dateDaysAgo = (days, hour) => {
    const date = new Date();
    date.setDate(date.getDate() - days);
    date.setHours(hour, 20, 0, 0);
    return date.toISOString();
};

export const mockOrders = [
    {
        id: 1048,
        clienteId: 1,
        status: "EM_PREPARO",
        total: 65.8,
        criadoEm: dateDaysAgo(0, 12),
        itens: [
            { produtoId: 1, nome: "X-Burger da casa", precoUnitario: 24.9, quantidade: 2, subtotal: 49.8 },
            { produtoId: 3, nome: "Suco de laranja", precoUnitario: 8, quantidade: 2, subtotal: 16 },
        ],
    },
    {
        id: 1047,
        clienteId: 1,
        status: "PRONTO",
        total: 34,
        criadoEm: dateDaysAgo(0, 11),
        itens: [
            { produtoId: 2, nome: "Batata crocante", precoUnitario: 12.5, quantidade: 1, subtotal: 12.5 },
            { produtoId: 4, nome: "Cheeseburger", precoUnitario: 21.5, quantidade: 1, subtotal: 21.5 },
        ],
    },
    {
        id: 1046,
        clienteId: 1,
        status: "ENTREGUE",
        total: 43,
        criadoEm: dateDaysAgo(1, 19),
        itens: [
            { produtoId: 4, nome: "Cheeseburger", precoUnitario: 21.5, quantidade: 2, subtotal: 43 },
        ],
    },
];
