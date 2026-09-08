# Regras de Negócio — Autenticação e Usuários

## 1. Objetivo

Este documento registra as regras de negócio definidas para o módulo de **Autenticação, Usuários e Supabase** do Gerenciador de Lanchonete.

O módulo é responsável por:
- integração com o Supabase Auth;
- cadastro e autenticação de usuários;
- gestão dos perfis Cliente, Funcionário e Admin;
- regras de autorização e permissões;
- controle de usuários ativos e desativados.

---

## 2. Perfis do sistema

O sistema possui três perfis principais:

- `CLIENTE`
- `FUNCIONARIO`
- `ADMIN`

Cada usuário possui uma única role principal.

---

## 3. Regras de negócio

### RN-AUTH-01 — Autenticação por credenciais

O usuário somente poderá ser autenticado quando as credenciais informadas forem válidas.

A validação das credenciais será delegada ao **Supabase Auth**.

**Comportamento esperado:**
- credenciais válidas → autenticação realizada;
- credenciais inválidas → autenticação rejeitada.

**Testes unitários previstos:**
- deve autenticar com credenciais válidas;
- deve rejeitar credenciais inválidas;
- deve rejeitar e-mail vazio;
- deve rejeitar senha vazia.

---

### RN-AUTH-02 — Token de autenticação

Após uma autenticação bem-sucedida, o Supabase Auth fornece um JWT que será utilizado nas requisições protegidas da aplicação.

O backend deverá utilizar esse token para identificar o usuário autenticado.

> A validação do JWT pelo filtro de segurança será implementada posteriormente. Ela não faz parte dos testes unitários de domínio deste escopo.

---

### RN-USER-01 — Usuário deve possuir uma role

Todo usuário da aplicação deve possuir exatamente uma role válida:

- `CLIENTE`;
- `FUNCIONARIO`;
- `ADMIN`.

Não deve existir usuário da aplicação sem uma role definida.

**Teste unitário previsto:**
- deve criar/manter usuário com uma role válida.

---

### RN-USER-02 — Novo usuário inicia como CLIENTE

Todo usuário criado por meio do cadastro comum deve receber automaticamente a role `CLIENTE`.

O usuário não poderá escolher `ADMIN` ou `FUNCIONARIO` durante o cadastro comum.

**Exemplos proibidos:**

```json
{
  "nome": "Usuário",
  "email": "usuario@email.com",
  "role": "ADMIN"
}
```

```json
{
  "nome": "Usuário",
  "email": "usuario@email.com",
  "role": "FUNCIONARIO"
}
```

**Testes unitários previstos:**
- novo usuário deve receber role `CLIENTE`;
- usuário não pode definir `ADMIN` durante o cadastro;
- usuário não pode definir `FUNCIONARIO` durante o cadastro.

---

### RN-USER-03 — E-mail deve ser único

O e-mail utilizado para cadastro deve identificar um único usuário.

A autenticação das credenciais será responsabilidade do Supabase Auth, que também será utilizado na integração de autenticação da aplicação.

---

### RN-USER-04 — Usuário deve possuir status

O perfil do usuário deve possuir um indicador de situação:

- `ativo = true` → usuário ativo;
- `ativo = false` → usuário desativado.

---

### RN-USER-05 — Usuário desativado não pode acessar recursos protegidos

Um usuário desativado não poderá utilizar recursos protegidos da aplicação, mesmo que possua um token de autenticação válido.

**Comportamento esperado:**

```text
JWT válido + usuário ativo
→ acesso permitido

JWT válido + usuário desativado
→ acesso negado
```

**Testes unitários previstos:**
- usuário ativo pode acessar recursos;
- usuário desativado não pode acessar recursos.

---

### RN-AUTHZ-01 — Somente ADMIN pode gerenciar usuários

Apenas usuários com role `ADMIN` podem realizar operações de gerenciamento de usuários.

Isso inclui, no escopo atual:

- consultar usuários;
- criar/cadastrar funcionários;
- alterar roles;
- desativar usuários.

**Testes unitários previstos:**
- ADMIN pode gerenciar usuários;
- CLIENTE não pode gerenciar usuários;
- FUNCIONARIO não pode gerenciar usuários.

---

### RN-AUTHZ-02 — Somente ADMIN pode alterar roles

A alteração da role de um usuário é uma operação administrativa.

Somente `ADMIN` pode alterar a role de outro usuário.

**Comportamento esperado:**

```text
ADMIN       → pode alterar role
FUNCIONARIO → não pode alterar role
CLIENTE     → não pode alterar role
```

**Testes unitários previstos:**
- ADMIN pode alterar role;
- CLIENTE não pode alterar role;
- FUNCIONARIO não pode alterar role.

---

### RN-AUTHZ-03 — Somente ADMIN pode desativar usuários

A desativação de usuários é uma operação administrativa.

Somente `ADMIN` pode desativar um usuário.

**Comportamento esperado:**

```text
ADMIN       → pode desativar usuário
FUNCIONARIO → não pode desativar usuário
CLIENTE     → não pode desativar usuário
```

---

### RN-AUTHZ-04 — Usuário pode acessar o próprio perfil

Um usuário autenticado pode consultar e atualizar os dados permitidos do seu próprio perfil.

O usuário não pode utilizar essa permissão para alterar sua própria role.

A alteração de roles permanece restrita ao `ADMIN`.

---

## 4. Matriz inicial de permissões

| Operação | CLIENTE | FUNCIONARIO | ADMIN |
|---|:---:|:---:|:---:|
| Acessar próprio perfil | ✅ | ✅ | ✅ |
| Alterar dados permitidos do próprio perfil | ✅ | ✅ | ✅ |
| Consultar usuários | ❌ | ❌ | ✅ |
| Criar funcionário | ❌ | ❌ | ✅ |
| Alterar role | ❌ | ❌ | ✅ |
| Desativar usuário | ❌ | ❌ | ✅ |
| Gerenciar usuários | ❌ | ❌ | ✅ |


## 5. Observação sobre o Supabase

O Supabase será utilizado como provedor de autenticação e como infraestrutura de persistência do projeto.

A aplicação Spring Boot continuará responsável pela lógica de negócio, regras de usuários e autorização.

A divisão conceitual é:

```text
Supabase Auth
    ↓
Autenticação / credenciais / JWT

Spring Boot
    ↓
Regras de negócio
    ↓
Usuários / roles / permissões
    ↓
Autorização da aplicação
```
