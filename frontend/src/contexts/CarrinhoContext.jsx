import { useCallback, useEffect, useMemo, useState } from "react";
import { initialCart } from "../data/mockStore";
import { CarrinhoContext } from "./useCarrinho";

const CHAVE_CARRINHO = "lanchonete.carrinho";

function itemValido(item) {
    return (
        item !== null &&
        typeof item === "object" &&
        Number.isInteger(item.id) &&
        typeof item.nome === "string" &&
        (item.descricao === null || typeof item.descricao === "string") &&
        Number.isFinite(item.preco) &&
        item.preco >= 0 &&
        Number.isInteger(item.quantidade) &&
        item.quantidade > 0
    );
}

function calcularTotal(itens) {
    const total = itens.reduce((soma, item) => soma + item.preco * item.quantidade, 0);
    return Math.round((total + Number.EPSILON) * 100) / 100;
}

function lerCarrinho() {
    const salvo = localStorage.getItem(CHAVE_CARRINHO);
    if (salvo === null) return initialCart;

    let dadosSalvos;
    try {
        dadosSalvos = JSON.parse(salvo);
    } catch (error) {
        console.error("Carrinho salvo inválido; usando o carrinho inicial.", error);
        localStorage.removeItem(CHAVE_CARRINHO);
        return initialCart;
    }

    const carrinho = Array.isArray(dadosSalvos) ? dadosSalvos : dadosSalvos?.itens;
    if (!Array.isArray(carrinho) || !carrinho.every(itemValido)) {
        console.error("Carrinho salvo fora do formato esperado; usando o carrinho inicial.");
        localStorage.removeItem(CHAVE_CARRINHO);
        return initialCart;
    }

    return carrinho;
}

function produtoValido(produto) {
    return (
        produto !== null &&
        typeof produto === "object" &&
        Number.isInteger(produto.id) &&
        typeof produto.nome === "string" &&
        (produto.descricao === null || typeof produto.descricao === "string") &&
        Number.isFinite(produto.preco) &&
        produto.preco >= 0
    );
}

export function CarrinhoProvider({ children }) {
    const [cart, setCart] = useState(lerCarrinho);
    const total = calcularTotal(cart);

    useEffect(() => {
        try {
            localStorage.setItem(CHAVE_CARRINHO, JSON.stringify({ itens: cart, total }));
        } catch (error) {
            console.error("Não foi possível salvar o carrinho no armazenamento local.", error);
        }
    }, [cart, total]);

    useEffect(() => {
        const sincronizarOutraAba = (evento) => {
            if (evento.key === null || evento.key === CHAVE_CARRINHO) {
                setCart(lerCarrinho());
            }
        };
        window.addEventListener("storage", sincronizarOutraAba);
        return () => window.removeEventListener("storage", sincronizarOutraAba);
    }, []);

    const adicionarItem = useCallback((produto) => {
        if (!produtoValido(produto)) {
            throw new Error("Produto inválido: id, nome, descrição e preço são obrigatórios.");
        }

        setCart((carrinhoAtual) => {
            const existente = carrinhoAtual.find((item) => item.id === produto.id);
            if (existente) {
                return carrinhoAtual.map((item) =>
                    item.id === produto.id
                        ? { ...item, quantidade: item.quantidade + 1 }
                        : item
                );
            }

            return [
                ...carrinhoAtual,
                {
                    id: produto.id,
                    nome: produto.nome,
                    descricao: produto.descricao,
                    preco: produto.preco,
                    quantidade: 1,
                },
            ];
        });
    }, []);

    const atualizarQuantidade = useCallback((productId, quantity) => {
        if (!Number.isInteger(quantity) || quantity < 0) {
            throw new Error("A quantidade deve ser um número inteiro não negativo.");
        }

        setCart((carrinhoAtual) => carrinhoAtual
            .map((item) => item.id === productId ? { ...item, quantidade: quantity } : item)
            .filter((item) => item.quantidade > 0));
    }, []);

    const limparCarrinho = useCallback(() => setCart([]), []);

    const valor = useMemo(
        () => ({
            cart,
            total,
            adicionarItem,
            atualizarQuantidade,
            limparCarrinho,
        }),
        [cart, total, adicionarItem, atualizarQuantidade, limparCarrinho]
    );

    return (
        <CarrinhoContext.Provider value={valor}>
            {children}
        </CarrinhoContext.Provider>
    );
}
