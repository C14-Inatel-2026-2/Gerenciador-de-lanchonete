# 🍔 Gerenciador de Lanchonete

O **Gerenciador de Lanchonete** é uma aplicação web desenvolvida como projeto prático para a matéria de **Engenharia de Software**. O sistema permite que clientes realizem pedidos a partir do cardápio digital, enquanto funcionários e administradores gerenciam a fila da cozinha, produtos, categorias e métricas da lanchonete.
Perfis de usuário: Cliente, Funcionário (cozinha) e Administrador.

---

## 👥 Equipe e Divisão de Papéis

| Integrante   | Papel no Projeto / Módulo | Atribuições Principais                                                                                    |
| :----------- | :------------------------ | :-------------------------------------------------------------------------------------------------------- |
| **Karolina** | Autenticação & Usuários   | Gestão de perfis (Cliente, Funcionário, Admin), integração com Supabase Auth e segurança da API.          |
| **Beatriz**  | Catálogo & Produtos       | Endpoints REST de CRUD para Produtos e Categorias, controle de estoque e filtros do cardápio.             |
| **Júlia**    | Carrinho & Pedidos        | Gestão do carrinho de compras, cálculo de totais e persistência inicial dos pedidos.                      |
| **Lilyan**   | Ciclo de Vida do Pedido   | Transições da máquina de estados do pedido (`CRIADO` $\rightarrow$ `ENTREGUE`), cancelamento e histórico. |
| **Yasmin**   | Admin, Front-end & CI/CD  | Métricas/relatórios administrativos, integração com o Front-end React e orquestração da pipeline Jenkins. |

---

## 🛠️ Tecnologias Utilizadas (Stack)

- **Back-end:** Java 17 + Spring Boot
- **Gerenciador de Dependências:** Apache Maven
- **Testes Unitários:** JUnit 5 + Mockito
- **Banco de Dados & Autenticação:** Supabase (PostgreSQL + Auth API)
- **Front-end:** React + JavaScript
- **CI/CD:** Jenkins (Pipeline multi-job)
- **Hospedagem:** Render (Back-end) + Vercel (Front-end)

---

## 🚀 Deploy

### 🌐 Aplicação publicada

- **Frontend (Vercel):** https://gerenciador-de-lanchonete.vercel.app
- **Backend (Render):** https://gerenciador-de-lanchonete.onrender.com

O deploy automático é executado pelo Jenkins somente na branch `main`, após a conclusão bem-sucedida das etapas de build e testes.

Os Deploy Hooks do Render e da Vercel são credenciais privadas e devem ser configurados por quem administra o deploy no Jenkins.

Variáveis utilizadas pelo deploy:

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
 ├── frontend/                # Aplicação React / JavaScript
 │    ├── src/
 │    └── package.json
 ├── Jenkinsfile              # Pipeline CI/CD
 └── README.md
```

---

## Como instalar

<!-- Grupo: consolidar os passos de instalação e configuração inicial. -->

### Pré-requisitos

- Java 17
- Node.js 22
- Docker

---

## Como executar

<!-- Grupo: consolidar e testar os passos de execução; cada responsável documenta as variáveis do seu módulo. -->

### Supabase (banco de dados e autenticação)

<!-- Karolina: validar os passos de autenticação e administrador inicial. -->

1. Crie um projeto Supabase para o ambiente local/de demonstração.
2. No painel do projeto, habilite o provedor de autenticação por e-mail em **Authentication → Providers → Email**. Para demonstração, desative a confirmação de e-mail ou use endereços de e-mail reais.
3. Em **Project Settings → API**, obtenha a **Project URL** e a **anon/public key**. Configure-as no arquivo `frontend/.env` usando os nomes indicados em `frontend/.env.example`.
4. Em **Project Settings → Database**, obtenha os dados de conexão PostgreSQL e configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` em `backend/.env`, conforme `backend/.env.example`.
5. Se o projeto usar assinatura JWT legada HS256, configure também o segredo JWT no back-end. Projetos com chaves assimétricas validam os tokens pelo JWKS do projeto; não configure nem publique segredos que não sejam necessários.

Não versione os arquivos `.env` nem compartilhe senhas ou segredos. A chave `service_role` do Supabase nunca deve ser usada no front-end.

### Back-end

1. Copie `backend/.env.example` para `backend/.env` e preencha os dados do banco.
2. Na pasta `backend`: `docker compose up --build -d`
3. Logs: `docker compose logs -f backend`

### Front-end

1. Na pasta `frontend`, instale as dependências com `npm install` (somente na primeira vez).
2. Inicie a interface com `npm run dev` e abra o endereço indicado pelo Vite (normalmente `http://localhost:5173`).

### ⚙️ CI/CD local (Jenkins)

O Jenkins roda em Docker, já configurado por código (`jenkins/casc.yaml`).
Imagem e plugins têm versões fixas (`jenkins/Dockerfile` e `jenkins/plugins.txt`)
para o build ser reproduzível.

1. Copie `.env.example` para `.env` na raiz e defina usuário e senha do Jenkins
   (e `JENKINS_PORT`, se a 8081 estiver ocupada).
2. Suba o Jenkins: `docker compose up -d --build`
3. Acesse `http://localhost:8081` (ou a porta definida no `.env`).
4. O job **Gerenciador de Lanchonete** é criado automaticamente e roda o
   `Jenkinsfile` de cada branch do repositório.

### Variáveis de ambiente

<!-- Responsáveis por back-end, front-end e CI/CD: conferir esta lista com os exemplos e manter os arquivos sincronizados. -->

Copie os arquivos de exemplo correspondentes para `.env` nos diretórios indicados e preencha os valores localmente:

| Arquivo de exemplo                                                                                                          | Variável                 | Uso                                                                       |
| --------------------------------------------------------------------------------------------------------------------------- | ------------------------ | ------------------------------------------------------------------------- |
| [`backend/.env.example`](backend/.env.example)                                                                              | `DB_URL`                 | URL JDBC do banco PostgreSQL.                                             |
| [`backend/.env.example`](backend/.env.example)                                                                              | `DB_USERNAME`            | Usuário do banco PostgreSQL.                                              |
| [`backend/.env.example`](backend/.env.example)                                                                              | `DB_PASSWORD`            | Senha do banco PostgreSQL.                                                |
| [`backend/.env.example`](backend/.env.example)                                                                              | `CORS_ORIGENS`           | Origens autorizadas a acessar a API, separadas por vírgula.               |
| `backend/.env.example` (após integração da [#55](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/issues/55)) | `SUPABASE_URL`           | URL do projeto Supabase usada pelo back-end.                              |
| `backend/.env.example` (após integração da [#55](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/issues/55)) | `ADMIN_EMAILS`           | E-mails que recebem o perfil `ADMIN`, separados por vírgula.              |
| `backend/.env.example` (somente para HS256)                                                                                 | `SUPABASE_JWT_SECRET`    | Segredo JWT legado; não necessário para projetos com chaves assimétricas. |
| [`frontend/.env.example`](frontend/.env.example)                                                                            | `VITE_API_URL`           | URL base da API do back-end.                                              |
| [`frontend/.env.example`](frontend/.env.example)                                                                            | `VITE_SUPABASE_URL`      | Project URL do Supabase usada pelo front-end.                             |
| [`frontend/.env.example`](frontend/.env.example)                                                                            | `VITE_SUPABASE_ANON_KEY` | Chave anon/public do Supabase usada pelo front-end.                       |
| [`.env.example`](.env.example)                                                                                              | `JENKINS_PORT`           | Porta local do Jenkins.                                                   |
| [`.env.example`](.env.example)                                                                                              | `JENKINS_ADMIN_ID`       | Usuário administrador local do Jenkins.                                   |
| [`.env.example`](.env.example)                                                                                              | `JENKINS_ADMIN_PASSWORD` | Senha do administrador local do Jenkins.                                  |
| [`.env.example`](.env.example)                                                                                              | `RENDER_DEPLOY_HOOK`     | Deploy Hook privado do Render, usado pelo Jenkins.                        |
| [`.env.example`](.env.example)                                                                                              | `VERCEL_DEPLOY_HOOK`     | Deploy Hook privado da Vercel, usado pelo Jenkins.                        |

---

## Como usar

<!-- Cada responsável: preencher o passo a passo do seu perfil. -->

### Cliente

**Cadastro e login (US01):**

1. Na tela **Entrar**, selecione **Cadastre-se**. Em **Criar conta**, informe nome, e-mail e senha (mínimo de 6 caracteres) e selecione **Criar conta**. Toda conta nova nasce com o perfil **Cliente** e já entra direto no **Cardápio**.
2. Se o e-mail já tiver conta, a tela mostra "E-mail já cadastrado". Nome vazio ou e-mail inválido também mostram o erro e nada é salvo.
3. Nas próximas vezes, entre com e-mail e senha em **Entrar**. Cada perfil abre na sua página inicial: cliente no **Cardápio**, funcionário na **Cozinha**. E-mail ou senha errados mostram "E-mail ou senha incorretos."; uma conta desativada pelo administrador tem o acesso negado.
4. O menu superior mostra **Olá, nome** e o botão **Sair**, que encerra a sessão neste navegador.

**Cardápio (US02 e US08):**

1. Acesse **Cardápio** para consultar os produtos disponíveis, agrupados por categoria, com descrição e preço.
2. Use **Filtrar por categoria** para restringir a lista; selecione **Todas as categorias** para remover o filtro.
3. Produtos sem estoque aparecem como **Esgotado** e não podem ser adicionados ao carrinho. Nos demais, selecione **Adicionar ao carrinho**.

**Carrinho e finalização do pedido (US03):**

1. Acesse **Carrinho** para consultar o estado atual do seu carrinho de compras, com os itens do pedido, quantidade, preço unitário e preço total. Caso esteja vazio, apresenta a mensagem "Seu carrinho está vazio.
Adicione algum produto da nossa seleção para começar."
2. Se o carrinho não estiver vazio, o botão **Finalizar pedido** é habilitado e deve ser utilizado para concluir o pedido.
3. Após finalizar o pedido com sucesso, o carrinho exibe o número do pedido e é esvaziado. Para acompanhar o histórico, o status, a data e o valor dos pedidos, acesse Meus pedidos pelo menu superior. Cada pedido pode ser expandido para ver os itens e o progresso do status. Caso ainda não tenha feito pedidos, a tela informa isso.

### Cozinha (funcionário)

1. Entre com uma conta de **funcionário** (ou admin) e acesse **Cozinha**.
2. A fila mostra os pedidos **Recebidos**, **Em preparo** e **Prontos**, do mais antigo para o mais novo, com itens, total e horário. A tela se atualiza sozinha a cada 10 segundos.
3. Use os botões de cada pedido: **Iniciar preparo** → **Marcar como pronto** → **Entregar**. Pedidos entregues saem da fila.
4. Para cancelar, use **Cancelar** (disponível só enquanto o pedido está Recebido ou Em preparo). Os itens voltam para o estoque.

### Administrador

<!-- Dividir por módulo: Karolina documenta usuários e promoção a funcionário; Beatriz, catálogo e estoque; Yasmin, painel e métricas. -->

**Usuários (US07):**

1. **Criar o primeiro administrador:** antes do primeiro cadastro, configure `ADMIN_EMAILS` no ambiente do back-end com o e-mail que será usado (separado por vírgulas, quando houver mais de um). Cadastre-se no sistema com esse mesmo e-mail; o perfil correspondente deve receber a role `ADMIN`. Consulte [Segurança da API com Supabase Auth (JWT) e administrador inicial #55](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/issues/55).
2. **Promover alguém a funcionário:** entre com uma conta `ADMIN` e acesse **Usuários** no menu. Na linha da pessoa, troque o **Perfil** para **Funcionário** e confirme. O mesmo vale para voltar a **Cliente** ou promover a **Administrador**. O próprio admin não pode alterar o seu perfil.
3. **Desativar e reativar:** na mesma tela, **Desativar** tira o acesso da conta sem apagá-la (os pedidos antigos continuam no histórico) e **Reativar** devolve o acesso com o mesmo perfil. Não existe exclusão de usuário.

**Catálogo e produtos (US06 e US08):**

1. Acesse **Gerenciar produtos** para consultar os produtos disponíveis.
2. Na seção **Categorias ativas**, cadastre uma categoria informando o nome e selecionando **Cadastrar categoria**. Para renomear uma categoria, selecione **Editar categoria** e depois **Salvar categoria**. Para removê-la das opções ativas, selecione **Desativar categoria**; categorias desativadas não são excluídas do banco.
3. Para cadastrar um produto, informe nome, descrição, preço, categoria e quantidade em estoque e selecione **Cadastrar produto**. É necessário haver uma categoria ativa cadastrada.
4. Para alterar os dados de um produto, selecione **Editar**, ajuste os campos e escolha **Salvar alterações**. Para retirar um produto do cardápio sem excluí-lo, selecione **Desativar**.
5. Para ajustar o estoque de um produto existente, informe uma quantidade inteira maior ou igual a zero em **Ajustar estoque** e selecione **Salvar estoque**. Estoque zero faz o produto aparecer como esgotado no cardápio.

---

## Funcionalidades

<!-- Cada responsável: resumir as funcionalidades do módulo e relacioná-las às histórias de usuário. -->

- **Autenticação e usuários (US01, US07):** o cliente cria a própria conta com nome, e-mail e senha e entra com e-mail e senha pelo Supabase Auth; toda conta nova nasce como CLIENTE, mesmo que o pedido tente outro perfil, e o primeiro administrador vem da variável `ADMIN_EMAILS`. Cada perfil vê só o menu e as páginas que lhe cabem. A API valida o token do Supabase em toda requisição: sem token ou com token inválido responde 401, e com perfil sem permissão responde 403. O administrador lista os usuários, altera o perfil (por exemplo, promove um cliente a funcionário), desativa e reativa contas; a conta desativada perde o acesso, mas continua no banco com seus pedidos. O admin não pode alterar o próprio perfil nem se desativar.
- **Catálogo e produtos (US02, US06, US08):** o cliente consulta o cardápio agrupado por categoria, filtra por categoria e vê produtos disponíveis e seus preços. O administrador cadastra, consulta, edita e desativa produtos e categorias. O estoque pode ser ajustado pelo administrador; produtos sem estoque aparecem como esgotados e não podem ser adicionados ao carrinho. Ao finalizar pedidos, o estoque é baixado e, nos cancelamentos previstos pelo fluxo de pedidos, é reposto.
- **Carrinho e pedidos (US03):** o cliente adiciona produtos do cardápio ao carrinho, altera ou remove quantidades, acompanha o total e mantém o carrinho salvo no navegador. A quantidade respeita o estoque informado pelo catálogo (US08). Ao finalizar, o sistema cria o pedido para o cliente autenticado com status inicial `CRIADO`, valida preços e estoque no backend e, se houver erro, preserva o carrinho. Após o sucesso, exibe o número do pedido e limpa o carrinho. Em **Meus pedidos**, o cliente consulta seu histórico e os status retornados pela API, atualizados pelo fluxo da cozinha (US04).
- **Fila da cozinha e status do pedido (US04):** o funcionário (ou admin) vê a fila da cozinha com os pedidos Recebidos, Em preparo e Prontos, do mais antigo para o mais novo, atualizada a cada 10 segundos. Cada pedido mostra só os botões das transições válidas (Iniciar preparo, Marcar como pronto, Entregar, Cancelar). A regra da máquina de estados fica no backend, sendo que uma transição inválida é recusada com 409 e o status não muda. Cancelar só é permitido enquanto o pedido está Recebido ou Em preparo. Ao cancelar, as quantidades dos itens voltam para o estoque.
- **Painel administrativo (US05):** o administrador visualiza um painel com o resumo do funcionamento da lanchonete, incluindo a quantidade de pedidos do dia, o faturamento diário (desconsiderando pedidos cancelados), a quantidade de produtos ativos e de usuários cadastrados. O painel também apresenta a lista de pedidos recentes com seus respectivos status e destaca quando existem produtos com estoque baixo. O acesso ao painel é restrito ao perfil ADMIN.

---

## Histórias de usuário

<!-- Cada responsável pelo módulo: preencher a rastreabilidade entre suas histórias, funcionalidades e requisitos. -->

| História | Perfil        | Funcionalidade                                                                                                                                         | Requisitos relacionados     |
| -------- | ------------- |--------------------------------------------------------------------------------------------------------------------------------------------------------| --------------------------- |
| US01     | Cliente       | Criar conta (nome, e-mail e senha no Supabase Auth), entrar e sair; o perfil nasce sempre CLIENTE e conta desativada não acessa as áreas protegidas.   | RF-AUT01–RF-AUT07           |
| US02     | Cliente       | Consultar o cardápio por categoria, filtrar produtos e identificar os disponíveis e seus preços.                                                       | RF-CAT01–RF-CAT05           |
| US03     | Cliente       | Adicionar produtos ao carrinho, alterar quantidades, acompanhar o subtotal e o total, finalizar pedidos e consultar o histórico e o status dos pedidos. | RF-CAR01–RF-CAR08           |
| US04     | Funcionário   | Ver a fila da cozinha (pedidos Recebidos, Em preparo e Prontos, do mais antigo para o mais novo) e avançar ou cancelar o pedido seguindo a máquina de estados; transições inválidas são recusadas e o cancelamento devolve o estoque. | RF-CIC01–RF-CIC06           |
| US05     | Administrador | Visualizar um painel administrativo com resumo do dia. Acesso restrito a administradores.                                                              | RF-ADM01–RF-ADM07           |
| US06     | Administrador | Cadastrar, consultar, editar e desativar produtos; cadastrar e editar categorias.                                                                      | RF-CAT06–RF-CAT09           |
| US07     | Administrador | Listar usuários, alterar o perfil (promover a funcionário), desativar e reativar contas, sem alterar a própria role.                                   | RF-AUT06, RF-AUT08–RF-AUT12 |
| US08     | Administrador | Ajustar o estoque; o sistema informa produtos esgotados, impede sua adição ao carrinho, baixa estoque nos pedidos e repõe nos cancelamentos previstos. | RF-CAT10                    |

---

## Refactorings

<!-- Cada integrante: preencher duas linhas com refactorings realizados. -->

| Integrante | Code smell                                                                                                                                                                                               | Refactoring aplicado                                                                                                                                 | Por quê                                                                                     | PR                                                                            |
| ---------- |----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| ---------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| Karolina   | Código duplicado: `podeGerenciarUsuarios`, `podeAlterarRole` e `podeDesativarUsuario` (e depois `podeGerenciarCatalogo`) tinham o mesmo corpo no `AuthorizationService`                                  | Extrair método: `private boolean ehAdminAtivo(UserProfile)`, e as 4 permissões de ADMIN passaram a delegar para ele                                  | Se a regra de administrador mudar, muda em um lugar só                                      | [#69](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/69) |
| Karolina   | Setup repetido: `new AuthorizationService()` em todos os testes do `AuthorizationServiceTest`; e nome enganoso `validarEmail` no `UserService` (validava **e** normalizava, com a regex solta no método) | Refatorar teste: campo `service` criado no `@BeforeEach`; renomear para `normalizarEValidarEmail` e extrair a regex para a constante `FORMATO_EMAIL` | Menos repetição, testes focados no que verificam, e o nome passa a dizer o que o método faz | [#69](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/69) |
| Lilyan     | Código duplicado: a regra de transição de status era validada no `PedidoLifecycleService.atualizarStatus` e de novo em `Pedido.alterarStatus`; e `Pedido.setStatus` era público, sem uso, e permitia pular a máquina de estados | Remover a validação duplicada do service (a regra fica só em `Pedido.alterarStatus`) e remover o `setStatus` sem uso | A regra de transição fica em um lugar só, e não sobra um caminho para mudar o status sem validação | [#21](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/21) |
| Lilyan     | Código morto: `avancarParaPreparo`, `marcarComoPronto`, `entregar` e `cancelarPedido` no `PedidoLifecycleService` não eram usados (a API usa só `atualizarStatusPorId`) | Remover os 4 métodos sem uso; os testes passaram a exercitar o fluxo real por `atualizarStatus` | Menos código para manter e para confundir: há um único caminho para mudar o status | [#84](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/84) |
| Beatriz    | `ProdutoService.validarDados` faz tudo junto: o método valida, no mesmo bloco, se o nome foi informado, se o preço é válido e se a categoria foi informada.                                              | Extrair `validarNome(nome)`, `validarPreco(preco)` e `validarCategoria(categoriaId)`, mantendo `validarDados` como método coordenador e preservando a ordem atual: nome, preço e categoria. | Cada regra fica com nome próprio e pode ser entendida e mantida separadamente.              | [#93](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/93) |
| Beatriz    | Setup repetido no `ProdutoServiceTest`: `new Categoria("Lanches")` e `new Produto("X-Burguer", "Delicioso", ..., categoria)` copiados em quase todos os testes.                                          | `@beforeeach` criando a categoria e um método auxiliar `produtoComPreco(BigDecimal preco)`.                                                                                                 | Os testes ficam mostrando só o que muda em cada cenário.                                    | [#93](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/93) |
| Júlia      | Código duplicado: método `validarQuantidade` em `ItemCarrinho` e `ItemPedido` e método `validarProdutoId` em `CarrinhoCompras` e `ItemPedido`. | Criação da classe `ValidacoesCompras` para centralizar os métodos mencionados e usá-los nas classes. | A mesma regra fica em somente um lugar e não corre o risco de divergir. | [#70](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/70) |
| Júlia      | Código duplicado na preparação de dados de teste: vários testes criavam `Produto` repetindo a configuração de `nome`, `descrição`, `preço`, `categoria` e `ID`. | Criação da classe `ProdutoTestFactory` para centralizar essa preparação e manter os dados necessários visíveis em cada cenário de teste. | Apenas um lugar para montar produtos a serem utilizados nos testes de compras, evitando divergências. | [#70](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/70) |
| Yasmin     | Código duplicado: os rótulos de status dos pedidos eram mantidos em mais de um componente (AdminDashboard, Cozinha e OrdersPage).                                                                        | Extrair constante compartilhada ROTULO_STATUS para utils/statusPedido.js e reutilizar o mesmo mapeamento nos componentes.                            | Evita duplicação de código, mantém os rótulos consistentes entre as telas e facilita futuras alterações em um único lugar.                                 | #48                                                                              |
| Yasmin     | Componente AdminDashboard concentrava responsabilidades de exibição das métricas e da tabela de pedidos em um único arquivo.                                                                                                                                                                                                         | Extrair os componentes CardMetrica e TabelaPedidosRecentes, deixando o AdminDashboard responsável apenas pela composição da tela.     |  Melhora a organização do código, aumenta a reutilização dos componentes e facilita testes e manutenção.                                                                                           | #48                                                                              |

---

## Metodologia e Dinâmica de desenvolvimento

<!-- Grupo: complementar, se necessário, a descrição da metodologia e da dinâmica. -->

- [Metodologia de desenvolvimento](docs/METODOLOGIA.md)

- [Dinâmica de desenvolvimento](docs/DINAMICA.md)

## Requisitos Funcionais

> Cada integrante preenche a tabela do seu módulo. Todo RF referencia uma história de usuário (US). Formato do ID: `RF-<MÓDULO><nº>`.

### Autenticação e usuários (US01 e US07)

| ID       | Requisito funcional                                                                                                         | História   |
| -------- | --------------------------------------------------------------------------------------------------------------------------- | ---------- |
| RF-AUT01 | O sistema deve permitir que o cliente crie uma conta informando nome, e-mail e senha.                                       | US01       |
| RF-AUT02 | O sistema deve atribuir o perfil CLIENTE a toda conta criada pelo cadastro comum, ignorando qualquer outro perfil enviado.  | US01       |
| RF-AUT03 | O sistema deve recusar o cadastro de um e-mail já existente, informando "E-mail já cadastrado".                             | US01       |
| RF-AUT04 | O sistema deve validar o nome (obrigatório) e o formato do e-mail antes de salvar, guardando o e-mail em letras minúsculas. | US01       |
| RF-AUT05 | O sistema deve permitir que o usuário cadastrado entre no sistema e identificar o usuário logado em cada requisição.        | US01       |
| RF-AUT06 | O usuário logado deve poder consultar e atualizar os dados permitidos do próprio perfil, sem alterar a própria role.        | US01, US07 |
| RF-AUT07 | O sistema deve negar o acesso às áreas protegidas a usuários desativados.                                                   | US01       |
| RF-AUT08 | O administrador deve poder listar os usuários com nome, e-mail, perfil e situação (ativo ou desativado).                    | US07       |
| RF-AUT09 | O administrador deve poder promover a funcionário um usuário que já se cadastrou, alterando o perfil dele para FUNCIONÁRIO. | US07       |
| RF-AUT10 | O administrador deve poder alterar a role de um usuário.                                                                    | US07       |
| RF-AUT11 | O administrador deve poder desativar um usuário sem excluí-lo do banco.                                                     | US07       |
| RF-AUT12 | O administrador deve poder reativar um usuário desativado, que volta com o mesmo perfil de antes.                           | US07       |

### Catálogo (US02, US06 e US08)

| ID       | Requisito funcional                                                                                                                                               | História |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| RF-CAT01 | O sistema deve exibir o cardápio agrupado por categoria, com nome, descrição e preço em reais.                                                                    | US02     |
| RF-CAT02 | O sistema deve exibir no cardápio apenas os produtos disponíveis.                                                                                                 | US02     |
| RF-CAT03 | O sistema deve permitir filtrar os produtos por categoria.                                                                                                        | US02     |
| RF-CAT04 | O sistema deve informar que o cardápio está vazio quando não houver produtos disponíveis.                                                                         | US02     |
| RF-CAT05 | O sistema deve listar as categorias cadastradas.                                                                                                                  | US02     |
| RF-CAT06 | O administrador deve poder cadastrar, consultar e editar produtos.                                                                                                | US06     |
| RF-CAT07 | O administrador deve poder desativar um produto sem excluí-lo do banco.                                                                                           | US06     |
| RF-CAT08 | O administrador deve poder cadastrar e editar categorias.                                                                                                         | US06     |
| RF-CAT09 | O sistema deve validar os dados de produtos e categorias antes de salvar.                                                                                         | US06     |
| RF-CAT10 | O administrador deve poder informar e ajustar o estoque de produtos; o cardápio deve marcar produtos sem estoque como esgotados e impedir sua adição ao carrinho. | US08     |

### Carrinho e finalização do pedido (US03)

| ID       | Requisito funcional                                                                                                                                                                | História |
| -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| RF-CAR01 | O sistema deve permitir que o cliente adicione ao carrinho produtos disponíveis no cardápio, iniciando cada produto com quantidade 1.                                              | US03     |
| RF-CAR02 | Ao adicionar ao carrinho um produto que já esteja nele, o sistema deve somar a quantidade à linha existente, sem criar uma linha duplicada.                                        | US03     |
| RF-CAR03 | O sistema deve exibir, para cada item, o nome do produto, o preço unitário, a quantidade e o subtotal, além do total do carrinho em reais.                                         | US03     |
| RF-CAR04 | O sistema deve permitir alterar a quantidade de um item para um inteiro positivo e remover itens do carrinho, recalculando os valores após cada alteração.                         | US03     |
| RF-CAR05 | O sistema não deve permitir a finalização de um carrinho vazio nem a criação de pedido sem itens.                                                                                  | US03     |
| RF-CAR06 | Ao finalizar um carrinho válido, o sistema deve criar e persistir um pedido associado ao cliente, com os itens e quantidades informados, total calculado e status inicial CRIADO.  | US03     |
| RF-CAR07 | Após a confirmação de que o pedido foi salvo, o sistema deve exibir sua identificação e esvaziar o carrinho. Se a finalização falhar, deve informar o erro e preservar o carrinho. | US03     |
| RF-CAR08 | O backend deve validar a disponibilidade dos produtos e calcular o pedido com os preços do catálogo, sem confiar em preços enviados pelo front-end.                                | US03     |

### Fila da cozinha e status do pedido (US04)

| ID       | Requisito funcional                                                                                                                                           | História |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| RF-CIC01 | O funcionário deve poder avançar um pedido pelas etapas CRIADO → EM_PREPARO → PRONTO → ENTREGUE.                                                              | US04     |
| RF-CIC02 | O sistema deve permitir cancelar um pedido apenas quando ele está CRIADO ou EM_PREPARO.                                                                       | US04     |
| RF-CIC03 | O sistema deve recusar transições inválidas (pular etapa, voltar etapa ou sair de ENTREGUE/CANCELADO), sem alterar o status, informando a transição recusada. | US04     |
| RF-CIC04 | O sistema deve salvar o novo status do pedido a cada mudança válida.                                                                                          | US04     |
| RF-CIC05 | O funcionário deve poder ver a fila da cozinha com os pedidos CRIADO, EM_PREPARO e PRONTO, do mais antigo para o mais novo.                                   | US04     |
| RF-CIC06 | Apenas FUNCIONÁRIO e ADMIN devem poder alterar o status de um pedido.                                                                                         | US04     |

### Painel administrativo (US05)

| ID | Requisito funcional | História |
|----|---------------------|----------|
| RF-ADM01 | O sistema deve permitir que apenas usuários com perfil ADMIN acessem o painel administrativo. | US05 |
| RF-ADM02 | O painel deve exibir a quantidade de pedidos realizados no dia. | US05 |
| RF-ADM03 | O painel deve exibir o faturamento do dia desconsiderando pedidos cancelados. | US05 |
| RF-ADM04 | O painel deve exibir a quantidade de produtos ativos cadastrados. | US05 |
| RF-ADM05 | O painel deve exibir a quantidade de usuários cadastrados. | US05 |
| RF-ADM06 | O painel deve listar os pedidos mais recentes com seus respectivos status. | US05 |
| RF-ADM07 | O painel deve destacar quando existirem produtos com estoque baixo. | US05 |

| ID  | Requisito funcional | História |
| --- | ------------------- | -------- |

## Requisitos Não Funcionais

> Todo RNF precisa de uma métrica ou condição verificável. Formato do ID: `RNF-<MÓDULO><nº>`.

### Gerais (todo o sistema)

<!-- Júlia/grupo: ex.: portabilidade com Docker, usabilidade em celular -->

| ID        | Categoria     | Requisito não funcional                                                                                                                                                                                 | Como verificar                                                                                                                                        |
| --------- | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| RNF-GER01 | Portabilidade | O backend e o Jenkins podem ser iniciados com Docker Compose, conforme instruções do README; o front-end roda com npm install e npm run dev.                                                            | Seguir as instruções do README em uma máquina limpa e confirmar que os serviços sobem.                                                                |
| RNF-GER02 | Usabilidade   | As telas principais devem funcionar em larguras de viewport a partir de 360 px, sem rolagem horizontal e mantendo visíveis os conteúdos e controles necessários.                                        | Testar as telas em viewport de 360 px e verificar a ausência de rolagem horizontal e controles cortados.                                              |
| RNF-GER03 | Segurança     | Senhas, tokens e credenciais de serviços externos não devem ser armazenados no repositório; devem ser configurados por variáveis de ambiente.                                                           | Revisar os arquivos de configuração e verificar que usam variáveis de ambiente, sem valores secretos versionados.                                     |
| RNF-GER04 | Qualidade     | A pipeline do Jenkins deve executar build e testes automatizados do front-end e do back-end em cada branch do repositório (incluindo as branches de pull requests); uma falha deve reprovar a pipeline. | Abrir um pull request e verificar os resultados da pipeline; provocar uma falha de teste em ambiente de validação e confirmar que a pipeline reprova. |

### Catálogo

| ID        | Categoria        | Requisito não funcional                                                                                           | Como verificar                                                                                |
| --------- | ---------------- | ----------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| RNF-CAT01 | Desempenho       | A leitura do cardápio responde em até 1 s em 95% das requisições.                                                 | Medir com requisições repetidas no endpoint.                                                  |
| RNF-CAT02 | Segurança        | O cadastro e a edição de produtos e categorias só aceitam o perfil ADMIN (efetivo na Sprint 2, com o login real). | Teste em que outro perfil recebe acesso negado.                                               |
| RNF-CAT03 | Segurança        | As credenciais do banco vêm de variáveis de ambiente, sem segredos no repositório.                                | `application.properties` usa `${DB_URL}`, `${DB_USERNAME}` e `${DB_PASSWORD}`.                |
| RNF-CAT04 | Qualidade        | As classes de serviço do catálogo têm pelo menos 70% de cobertura de testes.                                      | Relatório JaCoCo no Jenkins.                                                                  |
| RNF-CAT05 | Integridade      | Produtos desativados permanecem no banco, preservando o histórico de pedidos.                                     | Teste que confirma que o produto desativado ainda existe.                                     |
| RNF-CAT06 | Manutenibilidade | O módulo separa model, repository, service e controller dentro do pacote do próprio módulo.                       | Estrutura de pastas do módulo.                                                                |
| RNF-CAT07 | Integridade      | O estoque de um produto nunca pode ficar negativo, inclusive em atualizações concorrentes.                        | Testes de domínio cobrem baixa acima do estoque e a entidade usa controle otimista de versão. |

### Autenticação e usuários

| ID        | Categoria        | Requisito não funcional                                                                                                                                                                                                       | Como verificar                                                                                                                                                                                    |
| --------- | ---------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| RNF-AUT01 | Segurança        | O backend não armazena senhas: a validação de credenciais fica com o provedor de autenticação (Supabase Auth).                                                                                                                | A entidade `UserProfile` não possui campo de senha.                                                                                                                                               |
| RNF-AUT02 | Segurança        | As operações de gerenciamento de usuários só aceitam o perfil ADMIN; CLIENTE e FUNCIONARIO recebem acesso negado (HTTP 403).                                                                                                  | Testes do `AuthorizationService` e dos endpoints de admin para cada perfil.                                                                                                                       |
| RNF-AUT03 | Segurança        | As chaves e URLs do Supabase vêm de variáveis de ambiente, sem segredos nos arquivos versionados.                                                                                                                             | Os arquivos atuais não contêm segredos e o `application.properties` usa variáveis `${...}`. Credenciais que já apareceram no histórico do Git são trocadas (como foi feito com a senha do banco). |
| RNF-AUT04 | Integridade      | O e-mail e o identificador do Supabase são únicos também no banco de dados, não apenas na validação do serviço.                                                                                                               | `UserProfile` usa `@Column(unique = true)` nesses campos.                                                                                                                                         |
| RNF-AUT05 | Qualidade        | `UserService` e `AuthorizationService` têm pelo menos 80% de cobertura de testes.                                                                                                                                             | Relatório JaCoCo no Jenkins.                                                                                                                                                                      |
| RNF-AUT06 | Manutenibilidade | A identificação do usuário logado fica isolada em uma única interface, de modo que trocar o mecanismo de login (por exemplo, de um login simplificado para o Supabase Auth) não altere controllers nem regras de autorização. | Controllers e `AuthorizationService` dependem apenas da interface, não da implementação.                                                                                                          |

### Carrinho e pedido

| ID        | Categoria      | Requisito não funcional                                                                                                                               | Como verificar                                                                                                                                |
| --------- | -------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| RNF-CAR01 | Integridade    | O total do pedido deve ser calculado no backend com valores decimais (BigDecimal), usando os preços do catálogo, com precisão de duas casas decimais. | Testes com múltiplos itens e quantidades; comparar os subtotais e o total persistido com o resultado esperado.                                |
| RNF-CAR02 | Confiabilidade | A falha ou rejeição da finalização não deve criar um pedido parcial nem limpar o carrinho.                                                            | Testes automatizados para produto indisponível, requisição inválida e falha ao salvar; confirmar pedido não persistido e carrinho preservado. |
| RNF-CAR03 | Desempenho     | A finalização do pedido deve responder em até 1 s em 95% de 50 requisições sequenciais.                                                               | Enviar 50 requisições sequenciais ao endpoint de criação de pedido e medir o tempo de resposta: pelo menos 48 devem responder em até 1 s.     |
| RNF-CAR04 | Qualidade      | As classes de serviço do módulo de carrinho e pedidos devem ter pelo menos 70% de cobertura de testes.                                                | Relatório JaCoCo gerado na pipeline Jenkins, seguindo o padrão já proposto para o catálogo no README.                                         |

### Cozinha e status do pedido

| ID        | Categoria          | Requisito não funcional                                                                                                              | Como verificar                                                                                              |
| --------- | ------------------ | ------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------- |
| RNF-CIC01 | Manutenibilidade   | A regra de transição de status fica em um único lugar (`StatusPedido.podeTransitarPara`, usada por `Pedido.alterarStatus`).          | Nenhum outro arquivo altera o status diretamente; refactor da PR #21.                                       |
| RNF-CIC02 | Confiabilidade     | Uma transição inválida não altera o status nem é salva no banco.                                                                     | Testes do `PedidoLifecycleServiceTest` com `verify(never()).save`.                                          |
| RNF-CIC03 | Qualidade          | Todas as combinações de transição (válidas e inválidas) são cobertas por testes automatizados.                                       | `StatusPedidoTest` (testes parametrizados) no relatório do Jenkins.                                         |
| RNF-CIC04 | Manutenibilidade   | O status é salvo no banco como texto (nome do enum), para que adicionar um novo status não corrompa pedidos antigos.                 | `Pedido` usa `@Enumerated(EnumType.STRING)`.                                                                |
| RNF-CIC05 | Interoperabilidade | A API responde HTTP 409 (Conflict) quando a transição de status é inválida.                                                          | `CozinhaControllerTest.patchComTransicaoInvalidaRetorna409` (MockMvc) no relatório do Jenkins.              |
| RNF-CIC06 | Segurança          | A fila da cozinha e a mudança de status só aceitam FUNCIONARIO e ADMIN; CLIENTE recebe 403 e nenhum pedido é consultado ou alterado. | Testes do `CozinhaControllerTest` (403 para CLIENTE) e do `AuthorizationServiceTest` (`podeOperarCozinha`). |

### Painel administrativo

| ID | Categoria | Requisito não funcional | Como verificar |
|----|-----------|-------------------------|----------------|
| RNF-ADM01 | Desempenho | O painel administrativo deve carregar o resumo do dia em até 1 segundo em condições normais de uso. | Medir o tempo da requisição para `/api/admin/resumo`. |
| RNF-ADM02 | Segurança | Apenas usuários com perfil ADMIN podem acessar o painel administrativo; outros perfis devem receber acesso negado. | Testar acesso com CLIENTE e FUNCIONÁRIO e verificar resposta de autorização. |
| RNF-ADM03 | Confiabilidade | Em caso de erro na consulta do resumo, o sistema deve informar a falha sem interromper o funcionamento da aplicação. | Simular indisponibilidade da API e verificar a mensagem de erro no painel. |
| RNF-ADM04 | Qualidade | O módulo administrativo deve possuir testes automatizados para os componentes principais do painel. | Executar `npm run test:run` e verificar os testes do AdminDashboard. |
| RNF-ADM05 | Manutenibilidade | Os rótulos de status utilizados pelo painel devem ser reutilizados a partir de um utilitário compartilhado (`statusPedido.js`), evitando duplicação de código. | Verificar que AdminDashboard, Cozinha e Meus Pedidos utilizam `ROTULO_STATUS` do utilitário compartilhado. |

| ID  | Categoria | Requisito não funcional | Como verificar |
| --- | --------- | ----------------------- | -------------- |

---

## Uso de IA

> O uso de IA foi permitido na disciplina. Declaramos como cada integrante usou, com exemplos reais do que foi aceito, ajustado ou descartado.

### Visão geral do grupo

| Integrante | Ferramentas                | Principais usos                                                                                                            |
| ---------- | -------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| Lilyan     | Claude e Gemini            | código e testes, revisão de PRs, investigação de erros, documentação, estudo dos conceitos                                 |
| Karolina | Claude Code | Código e testes do módulo de autenticação, testes de ponta a ponta com o Supabase, revisão de PRs, documentação |
| Beatriz    | Claude e Copilot           | Códigos e testes, compreensão de novos conceitos e ferramentas, investigação de erros, entendimento de Git, revisão de PRs |
| Júlia      | Copilot e ChatGPT          | Código e testes, investigação de erros, revisão do que era feito, questionamento sobre conceitos e ferramentas             |
| Yasmin     | ChatGPT   |                   Código e testes, investigação e resolução de erros, revisão de PRs, documentação, configuração de deploy (Render/Vercel/Jenkins), organização das entregas e compreensão de conceitos e ferramentas.                                                                                                         |

### Lilyan

**Como usei:** individualmente, no Claude, num projeto com o histórico do repositório (PRs, issues e quadro). Combinei uma regra com a IA de que **ela lê e analisa o repositório sem executar nada além disso; todo comando, teste, código e pipeline eu rodo na minha máquina sem que a IA rode virtualmente** e confiro o resultado. Usei também para revisar as PRs das colegas e para entender os conceitos de Engenharia de Software que precisamos aplicar no projeto.

**Exemplos reais**

| Pedido à IA (resumido)                             | O que a IA respondeu                                                                                                                                                                                                         | O que eu fiz                                                                                                                                                                                                                                                                                                                                                                                                               | Resultado      |
| -------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- |
| Montar o Jenkins local para a pipeline (#12)       | Gerou um `docker-compose` com a imagem oficial do Jenkins. Depois de subir, era preciso pegar a senha inicial, instalar plugins, criar o usuário e o job e trocar a branch à mão                                             | Comparei com o exemplo do monitor, "Jenkins como código", com imagem própria com Node e plugins pré-instalados, JCasC criando o usuário e o job multibranch sozinhos. Adaptei para o nosso projeto (Node em vez de Python, porta configurável, sem token por o repositório ser público). Depois, como montar do zero quebrava, fixei as versões e troquei para o JDK 21, porque o Jenkins não suporta mais o Java 17 (#18) | **Ajustado**   |
| Revisar a PR de refactorings da autenticação (#69) | Apontou que, depois do merge da `main`, o `podeGerenciarCatalogo` (que veio da PR #68) ficou com a regra de admin copiada, enquanto os outros métodos já usavam o `ehAdminAtivo`. Um conflito semântico, que o Git não acusa | Pedi o ajuste no review, e foi trocado para `ehAdminAtivo`                                                                                                                                                                                                                                                                                                                                                                 | **Aceito**     |
| Revisar a PR do Spring Security (#73)              | Sugeriu um teste "sem token - 401" em cada controller, porque todos os testes mandavam token e nenhum provava que a rota estava fechada                                                                                      | Levei a sugestão para o review e foi adicionado os testes                                                                                                                                                                                                                                                                                                                                                                  | **Aceito**     |
| Revisar a configuração do Supabase (#73)           | Levantou a suspeita de que as tabelas estavam abertas pela chave pública, porque estavam sem RLS                                                                                                                             | Conferi no banco (RLS desligado de fato) e testei com `curl` usando a chave pública, então o Supabase respondeu "permission denied". A suspeita não se confirmou                                                                                                                                                                                                                                                           | **Descartado** |

**O que fiz sem IA:** rodei todos os comandos, testes e pipelines na minha máquina e conferi os resultados; testei manualmente as telas, rotas e todas as PRs analisadas; tomei as decisões de organização, como propor o Trello e depois criar o quadro no GitHub Projects; realizei a divisão dos stages do Jenkins entre as integrantes; criei a regra de proteção da `main`; revisei e reescrevi todo texto gerado. O código do meu módulo foi gerado com apoio da IA, e eu revisei cada parte.

### Karolina

**Como usei:** utilizei o Claude Code dentro do VS Code, que trabalha direto no repositório: lê o código, edita os arquivos e roda os testes no meu terminal. Combinei que nada seria commitado ou enviado para o GitHub sem a minha autorização, e conferi cada mudança antes de subir. Quando havia mais de um caminho (regra de negócio, escopo ou ordem das tarefas), pedia para a IA me mostrar as opções e eu decidia. Também usei para revisar as PRs das colegas e para entender a diferença entre manter o `UsuarioAtualProvider` e usar o Spring Security completo antes de implementar a #55.

**Exemplos reais**

| Pedido à IA (resumido) | O que a IA respondeu | O que eu fiz | Resultado |
|---|---|---|---|
| Fazer o backend do painel administrativo (#48) | Apontou que o Render roda em UTC, então os pedidos seriam gravados com 3 horas a mais e o "dia" do painel viraria às 21h. Deu três opções para resolver | Escolhi fixar o fuso de Brasília na aplicação inteira, o que também corrigiu o horário da fila da cozinha | **Aceito** |
| Criar as telas de login e cadastro (#42) | Gerou as telas com as cores do template do Vite | Pedi para seguir a identidade visual que a Júlia já tinha criado no carrinho ([PR #59](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/59)) | **Ajustado** |
| Criar funções para reativar e deletar usuários (#43) | Fez o reativar e, antes do deletar, mostrou que ele contrariava a US07 (o usuário deve continuar no banco e nos pedidos antigos) e não apagaria a conta no Supabase | Decidi ficar só com desativar e reativar e documentei no `docs/api.md` que não existe exclusão | **Descartado** |
| Revisar as PRs da Lilyan (#72 e #74) | Rodou os testes e apontou um link quebrado no documento, uma linha fora da tabela e um `DELETE` que faltava no script de pedidos de teste, além de um detalhe opcional | Publiquei a revisão com os pontos importantes e deixei o detalhe opcional de fora | **Ajustado** |

**O que fiz sem IA:** configurei o projeto no Supabase; decidi as regras e o escopo de cada issue; testei as telas no navegador; revisei e aprovei cada commit, PR e comentário antes de publicar; e combinei com o grupo a ordem dos merges e as mudanças nos testes das colegas por causa do Spring Security. Num teste contra o banco do grupo, a IA deixou servidores rodando e ocupou as conexões do Supabase; depois disso, passei a exigir que todo servidor de teste fosse desligado ao final.

### Beatriz

**Como usei:** usei dois assistentes, o Copilot e o Claude, em papéis diferentes. O Copilot ficou mais próximo do código, porque enxergava os arquivos do projeto. Então eu usei para entender melhor os comentários de revisão das PRs, investigar testes que falhavam, aplicar ajustes pontuais no catálogo e entender o código das minhas colegas. O Claude usei mais para complementar a explicação de cada etapa, principalmente em configuração de ambiente, pipeline e Git. Como foi a minha primeira vez com branches, PRs e merges, muitas das minhas perguntas foram sobre esse fluxo.

**Exemplos reais**

| Pedido à IA (resumido)                                                                                 | O que a IA respondeu                                                                                                                                         | O que eu fiz                                                                                                                                                                                                              | Resultado    |
| ------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------ |
| Pedi ajuda para corrigir algumas implementações solicitadas na PR #68.                                 | Retornou possíveis correções para as implementações. Também ajudou a resolver o conflito no AuthorizationService, mantendo os métodos de catálogo e cozinha. | Revisei as sugestões e mantive as alterações dentro do escopo combinado. Rodei os testes relacionados e eles passaram.                                                                                                    | **Aceito**   |
| Corrigir os testes iniciais para o `CategoriaService` que eu havia feito.                              | Sugeriu correções para salvar categoria válida, rejeitar nome nulo, vazio, em branco ou duplicado e listar categorias.                                       | Avaliei os cenários, pedi para que me explicasse melhor os erros e suas respectivas correções no código que eu iniciei. Fiz as mudanças que estavam de acordo com o escopo e rodei os testes para validar.                | **Ajustado** |
| Como guardar o trabalho do filtro por categoria, ainda incompleto, para trocar de branch sem commitar. | Explicou o `git stash` para guardar as alterações e `git stash pop` para recuperá-las depois, e sugeriu manter um backup do trabalho.                        | Guardei o trabalho (`backup-wip-filtro`) e voltei a ele depois de terminar os controllers.                                                                                                                                | **Aceito**   |
| Como juntar os ajustes pedidos nas PRs #64, #67 e #68 sem abrir várias PRs novas.                      | Explicou o fluxo: fazer os ajustes na branch da #68, mergear só ela e fechar as outras como já incluídas.                                                    | Combinei com a colega, fiz os ajustes na branch da #68 e, depois do merge, fechei a #67 como incluída.                                                                                                                    | **Aceito**   |
| Montar o stage de testes do pipeline (JaCoCo + relatório JUnit), explicando cada ponto.                | Indicou como configurar o JaCoCo no `pom.xml` e o stage "Testes Backend" no Jenkinsfile, explicando cada trecho.                                             | Segui as etapas, executei o pipeline no meu Jenkins local, verifiquei o relatório e a cobertura (87,46%) e abri a PR #14 com prints como evidência. Também ajustei os relatórios dos testes Backend e Frontend no Jenkins | **Aceito**   |

**O que fiz sem IA:** rodei localmente os comandos e os testes e conferi os resultados; revisei cada sugestão, decidi quais eram adequadas ao escopo do projeto e reescrevi o código gerado com apoio da IA, quando necessário; testei as telas do meu módulo.

### Júlia

**Como usei:** utilizei majoritariamente o Copilot, que podia acessar os arquivos locais sem esgotar após poucas requisições e facilitava a análise. No início, o histórico das conversas não era mantido quando eu fechava a IDE. Dessa forma, sempre que iniciava um novo chat, definia que deveria primeiro análisar os arquivos considerando a minha pergunta e me entregar um plano de ação antes de qualquer execução, que só poderia ocorrer se fosse explicitamente solicitada. Também utilizei para compreender melhor o código das minhas colegas e algumas das minhas tarefas, reforçar comandos git e tirar dúvidas sobre as ferramentas utilizadas e sobre Engenharia de Software.

**Exemplos reais**

| Pedido à IA (resumido)                                                                                                                                                     | O que a IA respondeu                                                                                                                     | O que eu fiz                                                                                                                                                                                                                   | Resultado    |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------ |
| Analisar os métodos iniciais e verificar quais outros poderiam ser introduzidos no carrinho e nos pedidos.                                                                 | Sugeriu, por exemplo, um método para a validação dos itens e outro para a verificação da presença ou não de item específico no carrinho. | Avaliei o que tinha até o momento e se fazia sentido com nosso planejamento inicial.                                                                                                                                           | **Aceito**   |
| Fazer testes para o frontend a fim de verificar elementos que considerei necessários naquele momento.                                                                      | Criou os métodos específicos que solicitei e mais alguns.                                                                                | Como não sabia como os testes para frontend eram feitos, pedi para que me explicasse a ideia geral e como foi feito em cada teste. Analisei se faziam sentido e se seriam adequados ao planejamento.                           | **Aceito**   |
| Solicitei que me auxiliasse a implementar as adaptações recomendadas após a avaliação da [PR #59](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/59). | Como o frontend do cardápio ainda não estava implementado, uma das sugestões foi que eu fizesse uma parcela dessa implementação.         | Analisei se minha parte realmente precisaria disso e, entendendo que sim, mas sabendo que outra integrante estava encarregada deste módulo, optei por não fazer e apenas adaptar o mock utilizado para seguir o padrão da API. | **Ajustado** |

**O que fiz sem IA:** executei localmente os comandos e testes e conferi os resultados. Também naveguei pelas telas para procurar problemas que os testes não cobriam. Revisei e reescrevi, quando necessário, o código gerado com apoio da IA.

### Yasmin

<!-- Yasmin: ferramentas, como usou, 1 ou 2 exemplos (pedido → resposta → aceito/ajustado/descartado) e o que fez sem IA -->

---
