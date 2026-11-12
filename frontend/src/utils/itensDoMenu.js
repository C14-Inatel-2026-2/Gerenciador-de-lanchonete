export function itensDoMenu(role) {
  switch (role) {
    case "CLIENTE":
      return [
        { label: "Cardápio", path: "/cardapio" },
        { label: "Carrinho", path: "/carrinho" },
        { label: "Meus pedidos", path: "/meus-pedidos" },
      ];

    case "FUNCIONARIO":
      return [
        { label: "Cozinha", path: "/cozinha" },
      ];

    case "ADMIN":
      return [
        { label: "Cardápio", path: "/cardapio" },
        { label: "Carrinho", path: "/carrinho" },
        { label: "Meus pedidos", path: "/meus-pedidos" },
        { label: "Cozinha", path: "/cozinha" },
        { label: "Admin", path: "/admin" },
        { label: "Produtos", path: "/admin/produtos" },
        { label: "Usuários", path: "/admin/usuarios" },
      ];

    default:
      return [];
  }
}

/** Primeira página do menu do perfil: para onde vai quem acabou de entrar. */
export function paginaInicial(role) {
  return itensDoMenu(role)[0]?.path ?? "/login";
}
