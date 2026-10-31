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

| ID | Requisito funcional | História |
|----|---------------------|----------|
| RF-CAR01 | O sistema deve permitir que o cliente adicione ao carrinho produtos disponíveis no cardápio, iniciando cada produto com quantidade 1. | US03 |
| RF-CAR02 | Ao adicionar ao carrinho um produto que já esteja nele, o sistema deve somar a quantidade à linha existente, sem criar uma linha duplicada. | US03 |
| RF-CAR03 | O sistema deve exibir, para cada item, o nome do produto, o preço unitário, a quantidade e o subtotal, além do total do carrinho em reais. | US03 |
| RF-CAR04 | O sistema deve permitir alterar a quantidade de um item para um inteiro positivo e remover itens do carrinho, recalculando os valores após cada alteração. | US03 |
| RF-CAR05 | O sistema não deve permitir a finalização de um carrinho vazio nem a criação de pedido sem itens. | US03 |
| RF-CAR06 | Ao finalizar um carrinho válido, o sistema deve criar e persistir um pedido associado ao cliente, com os itens e quantidades informados, total calculado e status inicial CRIADO. | US03 |
| RF-CAR07 | Após a confirmação de que o pedido foi salvo, o sistema deve exibir sua identificação e esvaziar o carrinho. Se a finalização falhar, deve informar o erro e preservar o carrinho. | US03 |
| RF-CAR08 | O backend deve validar a disponibilidade dos produtos e calcular o pedido com os preços do catálogo, sem confiar em preços enviados pelo front-end. | US03 |

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
| RNF-GER01 | Portabilidade | O sistema deve poder ser iniciado em um ambiente compatível usando Docker Compose, conforme instruções documentadas no repositório, sem instalação manual das dependências da aplicação. | Seguir as instruções em um ambiente limpo e confirmar que os serviços iniciam corretamente. |
| RNF-GER02 | Usabilidade | As telas principais devem funcionar em larguras de viewport a partir de 360 px, sem rolagem horizontal e mantendo visíveis os conteúdos e controles necessários. | Testar as telas em viewport de 360 px e verificar a ausência de rolagem horizontal e controles cortados. |
| RNF-GER03 | Segurança | Senhas, tokens e credenciais de serviços externos não devem ser armazenados no repositório; devem ser configurados por variáveis de ambiente. | Revisar os arquivos de configuração e verificar que usam variáveis de ambiente, sem valores secretos versionados. |
| RNF-GER04 | Qualidade | A pipeline do Jenkins deve executar build e testes automatizados do front-end e do back-end em cada pull request; uma falha deve reprovar a pipeline. | Abrir um pull request e verificar os resultados da pipeline; provocar uma falha de teste em ambiente de validação e confirmar que a pipeline reprova. |

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

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|
| RNF-CAR01 | Integridade | O total do pedido deve ser calculado no backend com valores decimais (BigDecimal), usando os preços do catálogo, com precisão de duas casas decimais. | Testes com múltiplos itens e quantidades; comparar os subtotais e o total persistido com o resultado esperado. |
| RNF-CAR02 | Confiabilidade | A falha ou rejeição da finalização não deve criar um pedido parcial nem limpar o carrinho. | Testes automatizados para produto indisponível, requisição inválida e falha ao salvar; confirmar pedido não persistido e carrinho preservado. |
| RNF-CAR03 | Desempenho | A finalização do pedido deve responder em até 1 segundo em pelo menos 95% das requisições, sob uma carga de teste definida pelo grupo. | Teste de carga no endpoint de criação de pedido; registrar percentual de respostas dentro do limite e documentar a carga utilizada. |
| RNF-CAR04 | Qualidade | As classes de serviço do módulo de carrinho e pedidos devem ter pelo menos 70% de cobertura de testes. | Relatório JaCoCo gerado na pipeline Jenkins, seguindo o padrão já proposto para o catálogo no README. |

### Cozinha e status do pedido
<!-- Lilyan: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|

### Painel administrativo
<!-- Yasmin: adicione suas linhas abaixo -->

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|