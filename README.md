# 🍔 Gerenciador de Lanchonete

O **Gerenciador de Lanchonete** é uma aplicação web desenvolvida como projeto prático para a matéria de **Engenharia de Software**. O sistema permite que clientes realizem pedidos a partir do cardápio digital, enquanto funcionários e administradores gerenciam a fila da cozinha, produtos, categorias e métricas da lanchonete.
Perfis de usuário: Cliente, Funcionário (cozinha) e Administrador.
---

## 👥 Equipe e Divisão de Papéis

| Integrante | Papel no Projeto / Módulo | Atribuições Principais |
| :--- | :--- | :--- |
| **Karolina** | Autenticação & Usuários | Gestão de perfis (Cliente, Funcionário, Admin), integração com Supabase Auth e segurança da API. |
| **Beatriz** | Catálogo & Produtos | Endpoints REST de CRUD para Produtos e Categorias, controle de estoque e filtros do cardápio. |
| **Júlia** | Carrinho & Pedidos | Gestão do carrinho de compras, cálculo de totais e persistência inicial dos pedidos. |
| **Lilyan** | Ciclo de Vida do Pedido | Transições da máquina de estados do pedido (`CRIADO` $\rightarrow$ `ENTREGUE`), cancelamento e histórico. |
| **Yasmin** | Admin, Front-end & CI/CD | Métricas/relatórios administrativos, integração com o Front-end React e orquestração da pipeline Jenkins. |

---

## 🛠️ Tecnologias Utilizadas (Stack)

* **Back-end:** Java 17 + Spring Boot
* **Gerenciador de Dependências:** Apache Maven
* **Testes Unitários:** JUnit 5 + Mockito
* **Banco de Dados & Autenticação:** Supabase (PostgreSQL + Auth API)
* **Front-end:** React + TypeScript
* **CI/CD:** Jenkins (Pipeline multi-job)
* **Hospedagem:** Render (Back-end) + Vercel (Front-end)

---

## 🚀 Deploy e variáveis de ambiente

O deploy automático é executado pelo Jenkins somente na branch `main`, após a conclusão bem-sucedida das etapas de build e testes.

Os Deploy Hooks do Render e da Vercel são credenciais privadas e devem ser configurados por quem administra o deploy no Jenkins.

As variáveis necessárias são:
- `RENDER_DEPLOY_HOOK`
- `VERCEL_DEPLOY_HOOK`

Essas informações devem ser mantidas em segredo e nunca devem ser enviadas ao repositório.

---

## 📁 Estrutura do Monorepo

```text
gerenciador-lanchonete/
 ├── backend/                 # Aplicação Java / Spring Boot
 │    ├── src/
 │    └── pom.xml
 ├── frontend/                # Aplicação React / TypeScript
 │    ├── src/
 │    └── package.json
 ├── Jenkinsfile              # Pipeline CI/CD
 └── README.md
```

---

## ⚙️ CI/CD local (Jenkins)

O Jenkins roda em Docker, já configurado por código (`jenkins/casc.yaml`).
Imagem e plugins têm versões fixas (`jenkins/Dockerfile` e `jenkins/plugins.txt`)
para o build ser reproduzível.

1. Copie `.env.example` para `.env` na raiz e defina usuário e senha do Jenkins
   (e `JENKINS_PORT`, se a 8081 estiver ocupada).
2. Suba o Jenkins: `docker compose up -d --build`
3. Acesse `http://localhost:8081` (ou a porta definida no `.env`).
4. O job **Gerenciador de Lanchonete** é criado automaticamente e roda o
   `Jenkinsfile` de cada branch do repositório.

## Rodando o backend

1. Copie `backend/.env.example` para `backend/.env` e preencha os dados do banco.
2. Na pasta `backend`: `docker compose up --build -d`
3. Logs: `docker compose logs -f backend`

---

## Requisitos Funcionais

> Cada integrante preenche a tabela do seu módulo. Todo RF referencia uma história de usuário (US). Formato do ID: `RF-<MÓDULO><nº>`.

### Autenticação e usuários (US01 e US07)

| ID | Requisito funcional | História |
|----|---------------------|----------|
| RF-AUT01 | O sistema deve permitir que o cliente crie uma conta informando nome, e-mail e senha. | US01 |
| RF-AUT02 | O sistema deve atribuir o perfil CLIENTE a toda conta criada pelo cadastro comum, ignorando qualquer outro perfil enviado. | US01 |
| RF-AUT03 | O sistema deve recusar o cadastro de um e-mail já existente, informando "E-mail já cadastrado". | US01 |
| RF-AUT04 | O sistema deve validar o nome (obrigatório) e o formato do e-mail antes de salvar, guardando o e-mail em letras minúsculas. | US01 |
| RF-AUT05 | O sistema deve permitir que o usuário cadastrado entre no sistema e identificar o usuário logado em cada requisição. | US01 |
| RF-AUT06 | O usuário logado deve poder consultar e atualizar os dados permitidos do próprio perfil, sem alterar a própria role. | US01, US07 |
| RF-AUT07 | O sistema deve negar o acesso às áreas protegidas a usuários desativados. | US01 |
| RF-AUT08 | O administrador deve poder listar os usuários com nome, e-mail, perfil e situação (ativo ou desativado). | US07 |
| RF-AUT09 | O administrador deve poder cadastrar funcionários. | US07 |
| RF-AUT10 | O administrador deve poder alterar a role de um usuário. | US07 |
| RF-AUT11 | O administrador deve poder desativar um usuário sem excluí-lo do banco. | US07 |

### Catálogo (US02 e US06)

| ID | Requisito funcional | História |
|----|---------------------|----------|
| RF-CAT01 | O sistema deve exibir o cardápio agrupado por categoria, com nome, descrição e preço em reais. | US02 |
| RF-CAT02 | O sistema deve exibir no cardápio apenas os produtos disponíveis. | US02 |
| RF-CAT03 | O sistema deve permitir filtrar os produtos por categoria. | US02 |
| RF-CAT04 | O sistema deve informar que o cardápio está vazio quando não houver produtos disponíveis. | US02 |
| RF-CAT05 | O sistema deve listar as categorias cadastradas. | US02 |
| RF-CAT06 | O administrador deve poder cadastrar, consultar e editar produtos. | US06 |
| RF-CAT07 | O administrador deve poder desativar um produto sem excluí-lo do banco. | US06 |
| RF-CAT08 | O administrador deve poder cadastrar e editar categorias. | US06 |
| RF-CAT09 | O sistema deve validar os dados de produtos e categorias antes de salvar. | US06 |

### Carrinho e finalização do pedido (US03)
<!-- Júlia: adicione suas linhas abaixo -->

| ID | Requisito funcional | História |
|----|---------------------|----------|

### Fila da cozinha e status do pedido (US04)

| ID | Requisito funcional | História |
|----|---------------------|----------|
| RF-CIC01 | O funcionário deve poder avançar um pedido pelas etapas CRIADO → EM_PREPARO → PRONTO → ENTREGUE. | US04 |
| RF-CIC02 | O sistema deve permitir cancelar um pedido apenas quando ele está CRIADO ou EM_PREPARO. | US04 |
| RF-CIC03 | O sistema deve recusar transições inválidas (pular etapa, voltar etapa ou sair de ENTREGUE/CANCELADO), sem alterar o status, informando a transição recusada. | US04 |
| RF-CIC04 | O sistema deve salvar o novo status do pedido a cada mudança válida. | US04 |
| RF-CIC05 | O funcionário deve poder ver a fila da cozinha com os pedidos CRIADO, EM_PREPARO e PRONTO, do mais antigo para o mais novo. | US04 |
| RF-CIC06 | Apenas FUNCIONÁRIO e ADMIN devem poder alterar o status de um pedido. | US04 |

### Painel administrativo (US05)
<!-- Yasmin: adicione suas linhas abaixo -->

| ID | Requisito funcional | História |
|----|---------------------|----------|

## Requisitos Não Funcionais

> Todo RNF precisa de uma métrica ou condição verificável. Formato do ID: `RNF-<MÓDULO><nº>`.

### Gerais (todo o sistema)
<!-- Júlia/grupo: ex.: portabilidade com Docker, usabilidade em celular -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|

### Catálogo

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|
| RNF-CAT01 | Desempenho | A leitura do cardápio responde em até 1 s em 95% das requisições. | Medir com requisições repetidas no endpoint. |
| RNF-CAT02 | Segurança | O cadastro e a edição de produtos e categorias só aceitam o perfil ADMIN (efetivo na Sprint 2, com o login real). | Teste em que outro perfil recebe acesso negado. |
| RNF-CAT03 | Segurança | As credenciais do banco vêm de variáveis de ambiente, sem segredos no repositório. | `application.properties` usa `${DB_URL}`, `${DB_USER}` e `${DB_PASSWORD}`. |
| RNF-CAT04 | Qualidade | As classes de serviço do catálogo têm pelo menos 70% de cobertura de testes. | Relatório JaCoCo no Jenkins. |
| RNF-CAT05 | Integridade | Produtos desativados permanecem no banco, preservando o histórico de pedidos. | Teste que confirma que o produto desativado ainda existe. |
| RNF-CAT06 | Manutenibilidade | O módulo separa model, repository, service e controller dentro do pacote do próprio módulo. | Estrutura de pastas do módulo. |

### Autenticação e usuários

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|
| RNF-AUT01 | Segurança | O backend não armazena senhas: a validação de credenciais fica com o provedor de autenticação (Supabase Auth). | A entidade `UserProfile` não possui campo de senha. |
| RNF-AUT02 | Segurança | As operações de gerenciamento de usuários só aceitam o perfil ADMIN; CLIENTE e FUNCIONARIO recebem acesso negado (HTTP 403). | Testes do `AuthorizationService` e dos endpoints de admin para cada perfil. |
| RNF-AUT03 | Segurança | As chaves e URLs do Supabase vêm de variáveis de ambiente, sem segredos nos arquivos versionados. | Os arquivos atuais não contêm segredos e o `application.properties` usa variáveis `${...}`. Credenciais que já apareceram no histórico do Git são trocadas (como foi feito com a senha do banco). |
| RNF-AUT04 | Integridade | O e-mail e o identificador do Supabase são únicos também no banco de dados, não apenas na validação do serviço. | `UserProfile` usa `@Column(unique = true)` nesses campos. |
| RNF-AUT05 | Qualidade | `UserService` e `AuthorizationService` têm pelo menos 80% de cobertura de testes. | Relatório JaCoCo no Jenkins. |
| RNF-AUT06 | Manutenibilidade | A identificação do usuário logado fica isolada em uma única interface, de modo que trocar o mecanismo de login (por exemplo, de um login simplificado para o Supabase Auth) não altere controllers nem regras de autorização. | Controllers e `AuthorizationService` dependem apenas da interface, não da implementação. |

### Carrinho e pedido
<!-- Júlia: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|

### Cozinha e status do pedido

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|
| RNF-CIC01 | Manutenibilidade | A regra de transição de status fica em um único lugar (`StatusPedido.podeTransitarPara`, usada por `Pedido.alterarStatus`). | Nenhum outro arquivo altera o status diretamente; refactor da PR #21. |
| RNF-CIC02 | Confiabilidade | Uma transição inválida não altera o status nem é salva no banco. | Testes do `PedidoLifecycleServiceTest` com `verify(never()).save`. |
| RNF-CIC03 | Qualidade | Todas as combinações de transição (válidas e inválidas) são cobertas por testes automatizados. | `StatusPedidoTest` (testes parametrizados) no relatório do Jenkins. |
| RNF-CIC04 | Manutenibilidade | O status é salvo no banco como texto (nome do enum), para que adicionar um novo status não corrompa pedidos antigos. | `Pedido` usa `@Enumerated(EnumType.STRING)`. |
| RNF-CIC05 | Interoperabilidade | A API responde HTTP 409 (Conflict) quando a transição de status é inválida. | Teste do endpoint de status (Sprint 1). |

### Painel administrativo
<!-- Yasmin: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|
