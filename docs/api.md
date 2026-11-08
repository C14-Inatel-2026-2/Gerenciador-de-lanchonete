# Contrato da API REST

Contrato combinado entre front-end e back-end para que os módulos possam ser desenvolvidos em paralelo (issue #35).
Qualquer mudança de rota ou de formato deve ser atualizada aqui **no mesmo PR**.

## Convenções

- **Base URL:** `http://localhost:8080` (local) · URL do Render (produção).
- **Formato:** JSON (`Content-Type: application/json`) em todas as requisições e respostas.
- **Prefixo:** todas as rotas de negócio começam com `/api`. O CORS só é liberado para `/api/**`.
- **Health check:** `GET /actuator/health` → `200 { "status": "UP" }` (usado pelo Render).

### Usuário logado

Os controllers obtêm o usuário logado pela interface `UsuarioAtualProvider` (RNF-AUT06), nunca lendo headers diretamente.

| Fase | Como o front identifica o usuário |
|---|---|
| **Provisória (agora)** | Header `X-Usuario-Id: <id do UserProfile>` (`HeaderUsuarioAtualProvider`). |
| **Definitiva (#55)** | Header `Authorization: Bearer <access_token do Supabase>` (`SupabaseJwtUsuarioAtualProvider`). |

A troca não altera nenhum controller: só a implementação da interface.

#### Usuários de teste (fase provisória)

Com o header provisório, não dá para criar perfis pela API (o cadastro depende do token do Supabase). Para testar as rotas protegidas, rode o script [`backend/scripts/usuarios-teste.sql`](../backend/scripts/usuarios-teste.sql) no **SQL Editor** do Supabase. Ele cria:

| Id no banco do grupo | Usuário | E-mail | Perfil | Uso |
|---|---|---|---|---|
| `1` | Cliente Teste | `cliente.teste@lanchonete.com` | `CLIENTE` | Rotas de cliente (pedidos, perfil) |
| `2` | Funcionário Teste | `funcionario.teste@lanchonete.com` | `FUNCIONARIO` | Fila da cozinha e status do pedido |
| `3` | Admin Teste | `admin.teste@lanchonete.com` | `ADMIN` | Gestão de produtos, usuários e painel |
| `4` | Desativado Teste | `desativado.teste@lanchonete.com` | `CLIENTE` (inativo) | Conferir que o acesso é negado (`403`) |

**No banco compartilhado do grupo (Supabase), o script já foi rodado e os ids são os da tabela acima.** Em outro banco, os ids são gerados de novo e podem ser diferentes: confira pelo `SELECT` no fim do script. Exemplo:

```powershell
# Agindo como o Cliente Teste
curl.exe -i http://localhost:8080/api/usuarios/me -H "X-Usuario-Id: 1"
```

Depois da #55, esses perfis deixam de servir para login e podem ser removidos com o `DELETE` comentado no fim do script.

### Erros

Toda resposta de erro tem o mesmo corpo:

```json
{ "erro": "<mensagem legível para o usuário>" }
```

| Status | Quando | Exceção |
|---|---|---|
| `400` | Dados inválidos (validação de domínio, JSON malformado) | `IllegalArgumentException`, `UserValidationException` |
| `401` | Usuário não identificado (header/token ausente ou inválido) | `UsuarioNaoAutenticadoException` |
| `403` | Usuário sem permissão ou desativado | `AcessoNegadoException` |
| `404` | Recurso não existe | `RecursoNaoEncontradoException` |
| `409` | Conflito com o estado atual | `EmailJaCadastradoException`, `TransicaoStatusInvalidaException`, `EstoqueInsuficienteException` |
| `500` | Erro inesperado (detalhes só no log do servidor) | qualquer outra |

### Perfis

`CLIENTE`, `FUNCIONARIO`, `ADMIN`. A coluna **Acesso** abaixo indica quem pode chamar cada rota; os demais recebem `403`.

---

## Autenticação e perfil (US01 · #42)

Login, senha e recuperação de senha são feitos direto no **Supabase Auth** pelo front-end (#55). O back-end guarda apenas o perfil.

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `POST` | `/api/usuarios` | Usuário autenticado sem perfil | `{ "nome" }` | `201` `UsuarioResponse` · `409` se o perfil/e-mail já existe |
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

## Catálogo (US02 e US06 · #44, estoque #56)

| Método | Rota | Acesso | Corpo | Resposta |
|---|---|---|---|---|
| `GET` | `/api/categorias` | Público | — | `200` `CategoriaResponse[]` (só ativas) |
| `POST` | `/api/categorias` | ADMIN | `{ "nome" }` | `201` `CategoriaResponse` |
| `PUT` | `/api/categorias/{id}` | ADMIN | `{ "nome", "ativo" }` | `200` `CategoriaResponse` |
| `GET` | `/api/produtos?categoriaId={id}` | Público | — | `200` `ProdutoResponse[]` (só disponíveis; filtro opcional) |
| `POST` | `/api/produtos` | ADMIN | `ProdutoRequest` | `201` `ProdutoResponse` |
| `PUT` | `/api/produtos/{id}` | ADMIN | `ProdutoRequest` | `200` `ProdutoResponse` |
| `PATCH` | `/api/produtos/{id}/desativar` | ADMIN | — | `200` `ProdutoResponse` (`disponivel: false`) |
| `PATCH` | `/api/produtos/{id}/estoque` | ADMIN | `{ "quantidade": 20 }` | `200` `ProdutoResponse` |

```json
// CategoriaResponse
{ "id": 1, "nome": "Lanches", "ativo": true }

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
// ResumoResponse (dados do dia atual)
{ "totalPedidos": 12, "faturamento": 356.40,
  "pedidosPorStatus": { "CRIADO": 2, "EM_PREPARO": 1, "PRONTO": 0, "ENTREGUE": 8, "CANCELADO": 1 } }
```
