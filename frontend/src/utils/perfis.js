// Perfis de acesso: os mesmos do backend (UserRole). Lista única do front:
// se o backend ganhar um perfil novo, ele entra só aqui.
export const PERFIS = [
  { valor: "CLIENTE", rotulo: "Cliente" },
  { valor: "FUNCIONARIO", rotulo: "Funcionário" },
  { valor: "ADMIN", rotulo: "Administrador" },
];

export const ROLES = PERFIS.map((perfil) => perfil.valor);

export function rotuloDoPerfil(role) {
  return PERFIS.find((perfil) => perfil.valor === role)?.rotulo ?? role;
}
