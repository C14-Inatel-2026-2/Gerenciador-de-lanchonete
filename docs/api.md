# Contrato da API REST

Contrato combinado entre front-end e back-end para que os módulos possam ser desenvolvidos em paralelo (issue #35).
Qualquer mudança de rota ou de formato deve ser atualizada aqui **no mesmo PR**.

## Convenções

- **Base URL:** `http://localhost:8080` (local) · URL do Render (produção).
- **Formato:** JSON (`Content-Type: application/json`) em todas as requisições e respostas.
- **Prefixo:** todas as rotas de negócio começam com `/api`. O CORS só é liberado para `/api/**`.
- **Health check:** `GET /actuator/health` → `200 { "status": "UP" }` (usado pelo Render).

### Usuário logado

Login, cadastro e senha ficam no **Supabase Auth**. Toda rota que não é pública exige o token do Supabase (#55):

```
Authorization: Bearer <access_token da sessão do Supabase>
```

- O `SecurityConfig` valida o token **antes** de chegar ao controller: assinatura (chave pública do projeto, via JWKS), emissor (`<SUPABASE_URL>/auth/v1`), audiência `authenticated` e expiração. Sem token ou com token inválido → `401`.
- **Rotas públicas** (sem token): `GET /api/categorias`, `GET /api/produtos`, `GET /api/produtos/cardapio` e `GET /actuator/health`.
- Os controllers obtêm o usuário pela interface `UsuarioAtualProvider` (RNF-AUT06). A implementação `SupabaseJwtUsuarioAtualProvider` busca o perfil pelo `sub` do token: token válido sem perfil → `401`; perfil desativado → `403`.
- Quem é o usuário é decidido **só pelo token**. Ids enviados em header ou no corpo não dão acesso a nada (o header provisório `X-Usuario-Id` foi removido).

#### Primeiro administrador

Todo cadastro nasce `CLIENTE`. Os e-mails da variável `ADMIN_EMAILS` (separados por vírgula) nascem `ADMIN`, e esse admin promove os demais em `PATCH /api/usuarios/{id}/role`.

#### Variáveis do backend

| Variável | Exemplo | Uso |
|---|---|---|
| `SUPABASE_URL` | `https://<projeto>.supabase.co` | Validar o token. Sem ela, a aplicação não sobe. |
| `ADMIN_EMAILS` | `admin@admin.com` | E-mails que nascem ADMIN. |

Não há segredo do Supabase no backend: o token é validado com a chave **pública** do projeto.

#### Testando com `curl`

Pegue um token fazendo login direto no Supabase (a `anon key` é a *publishable key* do painel):

```powershell
curl.exe -s -X POST "https://<projeto>.supabase.co/auth/v1/token?grant_type=password" `
  -H "apikey: <anon key>" -H "Content-Type: application/json" `
  -d '{\"email\":\"voce@email.com\",\"password\":\"sua-senha\"}'
# copie o "access_token" da resposta (vale 1 hora)

curl.exe -i http://localhost:8080/api/usuarios/me -H "Authorization: Bearer <access_token>"
```

Os usuários de teste do script [`backend/scripts/usuarios-teste.sql`](../backend/scripts/usuarios-teste.sql) não têm conta no Supabase e **não servem mais para login**. Podem ser removidos com o `DELETE` comentado no fim do script.

### Erros

Toda resposta de erro tem o mesmo corpo:

```json
{ "erro": "<mensagem legível para o usuário>" }
```

| Status | Quando | Exceção |
|---|---|---|
| `400` | Dados inválidos (validação de domínio, JSON malformado) | `IllegalArgumentException`, `UserValidationException` |
| `401` | Token ausente, inválido ou expirado, ou usuário sem perfil | `SecurityConfig`, `UsuarioNaoAutenticadoException` |
| `403` | Usuário sem permissão ou desativado | `AcessoNegadoException` |
| `404` | Recurso não existe | `RecursoNaoEncontradoException` |
| `409` | Conflito com o estado atual | `EmailJaCadastradoException`, `TransicaoStatusInvalidaException`, `EstoqueInsuficienteException` |
| `500` | Erro inesperado (detalhes só no log do servidor) | qualquer outra |

### Perfis

`CLIENTE`, `FUNCIONARIO`, `ADMIN`. A coluna **Acesso** abaixo indica quem pode chamar cada rota; os demais recebem `403`.

---

## Autenticação e perfil (US01 · #42)

Login, senha e recuperação de senha são feitos direto no **Supabase Auth** pelo front-end. O back-end guarda apenas o perfil.

Fluxo do cadastro: o front chama `supabase.auth.signUp`, e com o token recebido chama `POST /api/usuarios`. No login: `signInWithPassword` e depois `GET /api/usuarios/me`.

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `POST` | `/api/usuarios` | Token do Supabase (ainda sem perfil) | `{ "nome" }` | `201` `UsuarioResponse` · `409` se o perfil/e-mail já existe |
| `GET` | `/api/usuarios/me` | Logado | — | `200` `UsuarioResponse` |
| `PATCH` | `/api/usuarios/me` | Logado | `{ "nome" }` | `200` `UsuarioResponse` (a role **nunca** é alterada aqui) |

O perfil criado em `POST /api/usuarios` é sempre `CLIENTE`, mesmo que o corpo traga `role`. O e-mail e o id do Supabase vêm do token, não do corpo.

```json
// UsuarioResponse
{ "id": 1, "nome": "Karolina", "email": "karolina@email.com", "role": "CLIENTE", "ativo": true }
```

## Administração de usuários (US07 · #43)

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `GET` | `/api/usuarios` | ADMIN | — | `200` `UsuarioResponse[]` |
| `PATCH` | `/api/usuarios/{id}/role` | ADMIN | `{ "role": "FUNCIONARIO" }` | `200` `UsuarioResponse` |
| `PATCH` | `/api/usuarios/{id}/desativar` | ADMIN | — | `200` `UsuarioResponse` (`ativo: false`) |

- `GET /api/usuarios` traz ativos e desativados, em ordem de nome.
- Para cadastrar um funcionário, ele se cadastra normalmente e o ADMIN muda a role dele para `FUNCIONARIO`.
- O ADMIN **não pode** alterar a própria role nem desativar a si mesmo → `403`.
- Usuário desativado continua no banco, mas perde o acesso às rotas protegidas (`403`).
- Erros: `400` role ausente ou inválida · `403` quem chama não é ADMIN ativo · `404` usuário não encontrado.

#### Passo a passo: criar um funcionário pelo `curl`

Enquanto a tela `/admin/usuarios` não existe, a promoção é feita pela API (PowerShell):

1. A pessoa se cadastra normalmente na tela `/cadastro` (ela nasce `CLIENTE`).
2. Pegue o token de um **ADMIN** (ver [Testando com `curl`](#testando-com-curl)) e guarde numa variável:
   ```powershell
   $token = "<access_token do admin>"
   $api = "http://localhost:8080"   # ou a URL do backend publicado
   ```
3. Descubra o `id` da pessoa:
   ```powershell
   curl.exe -s "$api/api/usuarios" -H "Authorization: Bearer $token"
   ```
4. Mude a role para `FUNCIONARIO` (troque `7` pelo id):
   ```powershell
   curl.exe -i -X PATCH "$api/api/usuarios/7/role" `
     -H "Authorization: Bearer $token" -H "Content-Type: application/json" `
     -d '{\"role\":\"FUNCIONARIO\"}'
   ```
   A resposta `200` já mostra `"role": "FUNCIONARIO"`.
5. A pessoa precisa **sair e entrar de novo** no site: o perfil (e o menu) é carregado no login.

## Catálogo (US02 e US06 · #44, estoque #56)

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `GET` | `/api/categorias` | Público | — | `200` `CategoriaResponse[]` (só ativas) |
| `POST` | `/api/categorias` | ADMIN | `{ "nome" }` | `201` `CategoriaResponse` |
| `PUT` | `/api/categorias/{id}` | ADMIN | `{ "nome", "ativo" }` | `200` `CategoriaResponse` |
| `PATCH` | `/api/categorias/{id}/desativar` | ADMIN | — | `200` `CategoriaResponse` (`ativo: false`) |
| `GET` | `/api/produtos/cardapio` | Público | — | `200` `CardapioResponse` (produtos disponíveis agrupados por categoria) |
| `GET` | `/api/produtos?categoriaId={id}` | Público | — | `200` `ProdutoResponse[]` (só disponíveis; filtro opcional) |
| `POST` | `/api/produtos` | ADMIN | `ProdutoRequest` | `201` `ProdutoResponse` |
| `PUT` | `/api/produtos/{id}` | ADMIN | `ProdutoRequest` | `200` `ProdutoResponse` |
| `PATCH` | `/api/produtos/{id}/desativar` | ADMIN | — | `200` `ProdutoResponse` (`disponivel: false`) |
| `PATCH` | `/api/produtos/{id}/estoque` | ADMIN | `{ "quantidade": 20 }` | `200` `ProdutoResponse` |

```json
// CategoriaResponse
{ "id": 1, "nome": "Lanches", "ativo": true }

// CardapioResponse
{
  "categorias": [
    {
      "id": 1,
      "nome": "Lanches",
      "produtos": [
        { "id": 10, "nome": "X-Burguer", "descricao": "Pão, carne e queijo", "preco": 18.90 }
      ]
    }
  ],
  "mensagem": null
}

// ProdutoRequest
{ "nome": "X-Burguer", "descricao": "Pão, carne e queijo", "preco": 18.90, "categoriaId": 1 }

// ProdutoResponse
{ "id": 10, "nome": "X-Burguer", "descricao": "Pão, carne e queijo", "preco": 18.90,
  "disponivel": true, "estoque": 20, "categoria": { "id": 1, "nome": "Lanches" } }
```

## Pedidos (US03 · #45)

O carrinho fica no front-end; o checkout envia os itens de uma vez.

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `POST` | `/api/pedidos` | CLIENTE | `PedidoRequest` | `201` `PedidoResponse` · `409` se faltar estoque |
| `GET` | `/api/pedidos/meus` | CLIENTE | — | `200` `PedidoResponse[]` (mais recentes primeiro) |
| `GET` | `/api/pedidos/{id}` | Dono do pedido, FUNCIONARIO, ADMIN | — | `200` `PedidoResponse` |

```json
// PedidoRequest
{ "itens": [ { "produtoId": 10, "quantidade": 2 } ] }

// PedidoResponse
{ "id": 5, "clienteId": 1, "status": "CRIADO", "total": 37.80, "criadoEm": "2026-11-04T19:30:00",
  "itens": [ { "produtoId": 10, "nome": "X-Burguer", "precoUnitario": 18.90, "quantidade": 2, "subtotal": 37.80 } ] }
```

O preço e o nome de cada item são copiados do produto no momento do pedido; o cliente não envia preço.

## Cozinha (US04 · #46)

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `GET` | `/api/pedidos/fila` | FUNCIONARIO, ADMIN | — | `200` `PedidoResponse[]` (status `CRIADO`, `EM_PREPARO`, `PRONTO`; mais antigos primeiro) |
| `PATCH` | `/api/pedidos/{id}/status` | FUNCIONARIO, ADMIN | `{ "status": "EM_PREPARO" }` | `200` `PedidoResponse` · `409` se a transição for inválida |

Transições permitidas: `CRIADO → EM_PREPARO | CANCELADO`, `EM_PREPARO → PRONTO | CANCELADO`, `PRONTO → ENTREGUE`.

## Painel administrativo (US05 · #48)

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `GET` | `/api/admin/resumo` | ADMIN | — | `200` `ResumoResponse` |

```json
// ResumoResponse
{
  "pedidosHoje": 12,
  "faturamentoHoje": 344.40,
  "ticketMedio": 31.31,
  "pedidosPorStatus": { "CRIADO": 2, "EM_PREPARO": 1, "PRONTO": 0, "ENTREGUE": 8, "CANCELADO": 1 },
  "produtosAtivos": 32,
  "usuariosCadastrados": 18,
  "pedidosRecentes": [
    { "id": 41, "cliente": "Maria", "status": "PRONTO", "total": 25.50, "criadoEm": "2026-11-12T19:30:00" }
  ]
}
```

- **"Hoje"** é o dia no fuso de Brasília (`America/Sao_Paulo`), de 00:00 até 23:59. A aplicação inteira roda nesse fuso (`FusoHorarioConfig`), então a hora gravada nos pedidos também é a de Brasília, mesmo no Render (UTC).
- `pedidosHoje` e `pedidosPorStatus` contam **todos** os pedidos do dia, inclusive os cancelados. `pedidosPorStatus` sempre traz os 5 status, com `0` quando não há pedidos.
- `faturamentoHoje` e `ticketMedio` **não** contam os pedidos `CANCELADO`. Sem vendas no dia, o ticket médio é `0`.
- `produtosAtivos`: produtos disponíveis no cardápio. `usuariosCadastrados`: todos os perfis, ativos e desativados.
- `pedidosRecentes`: os 5 pedidos mais recentes de **qualquer dia**, do mais novo para o mais antigo, com o nome do cliente.
- Erros: `401` sem token ou sem perfil · `403` quem chama não é ADMIN ativo.
- A lista `produtosComEstoqueBaixo` (#56) entra quando o campo de estoque do produto estiver na `main`.
