# 🍔 Gerenciador de Lanchonete

O **Gerenciador de Lanchonete** é uma aplicação web desenvolvida como projeto prático para a matéria de **Engenharia de Software**. O sistema permite que clientes realizem pedidos a partir do cardápio digital, enquanto funcionários e administradores gerenciam a fila da cozinha, produtos, categorias e métricas da lanchonete.

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