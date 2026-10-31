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

## Requisitos Funcionais

> Cada integrante preenche a tabela do seu módulo. Todo RF referencia uma história de usuário (US). Formato do ID: `RF-<MÓDULO><nº>`.

### Autenticação (US01)
<!-- Karolina: adicione suas linhas abaixo -->

| ID | Requisito funcional | História |
|----|---------------------|----------|

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
<!-- Lilyan: adicione suas linhas abaixo -->

| ID | Requisito funcional | História |
|----|---------------------|----------|

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

### Autenticação
<!-- Karolina: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|

### Carrinho e pedido
<!-- Júlia: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|

### Cozinha e status do pedido
<!-- Lilyan: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|

### Painel administrativo
<!-- Yasmin: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|