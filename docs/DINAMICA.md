# Dinâmica de desenvolvimento do grupo

## Divisão de tarefas

| Integrante | Módulo desenvolvido                                             |
| ---------- | --------------------------------------------------------------- |
| Beatriz    | Catálogo e produtos, com controle de estoque (US02, US06, US08) |
| Júlia      | Carrinho e pedidos (US03)                                       |
| Karolina   | Autenticação e usuários (US01, US07)                            |
| Lilyan     | Ciclo de vida do pedido e fila da cozinha (US04)                |
| Yasmin     | Base do front-end, painel administrativo (US05) e deploy        |

A divisão foi feita **por módulo de domínio**, na reunião do grupo antes da primeira entrega, com o intuito de combinar uma equilibrada divisão de tarefas, de modo a evitar que alguma integrante ficasse sobrecarregada. Cada integrante ficou responsável pelo seu módulo de ponta a ponta (regras, testes, API e tela), em vez de separar o grupo entre front-end e back-end. A ideia era equilibrar as tarefas e permitir que cada uma explicasse o próprio módulo por inteiro. Na entrega de CI/CD, seguimos a mesma lógica, onde cada integrante ficou com um stage do `Jenkinsfile`.

## Fluxo de branches

As branches continham um prefixo que indica o tipo de mudança: `feature/` (ou `feat/`), `fix/`, `ci/`, `docs/` e `refactor/`. Toda mudança chegava à main por Pull Request (PR).

A main era protegida por uma regra (ruleset) do GitHub, ou seja, nenhum PR podia ser mergeado sem **pelo menos uma aprovação** de outra integrante, e a aprovação era descartada se o código mudasse depois de aprovado. Além disso, a pipeline do Jenkins rodava os testes de cada branch antes do merge.

## Padrão de commits

O padrão escolhido para commits foi o **Conventional Commits** (`feat`, `fix`, `test`, `ci`, `docs`, `refactor`, `build`, `chore`), para deixar o histórico claro e fácil de ler. O padrão foi seguido na maior parte do histórico.

Exemplos reais:

- `ci: adiciona stage de testes com relatório JUnit e cobertura JaCoCo`
- `test(compras): adiciona testes unitários para modelos e serviços`
- `refactor(autenticacao): podeGerenciarCatalogo usa ehAdminAtivo`
- `docs: seção Uso de IA no README`
- `feat(frontend): inicializa aplicacao React`

## Code review

A fim de reduzir a integração de códigos e estruturas que não estivessem alinhados ao planejamento da plataforma, adotamos como regra a exigência de, pelo menos, uma aprovação em cada PR. Essa prática permitiu identificar e corrigir problemas durante o desenvolvimento, além de promover melhorias no código a partir das revisões realizadas. A título de exemplo, cita-se:

- **Testes de segurança:** na revisão da [PR #73](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/73) (Spring Security com o token do Supabase), foi pedido um teste "sem token → 401" em cada controller, porque os testes existentes sempre mandavam um usuário autenticado.

- **Regra de admin duplicada:** na revisão da [PR #69](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/69) (refactorings da autenticação), foi apontado que o `podeGerenciarCatalogo`, vindo de outra PR, repetia a regra que tinha acabado de ser centralizada (detalhes em "Conflitos e bloqueios").

- **Restauração de comentários:** [PR #18](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/18) e [PR #21](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/21).

- **Ajuste de Requisitos Não Funcionais (RNFs):** [PR #29](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/29).

## Conflitos e bloqueios

Durante o desenvolvimento, ocorreram casos em que os arquivos presentes no PR conflitavam com os presentes na Main, que havia sido atualizada. Com isso, foi necessário encontrar maneiras de resolvê-los, como nessas PRs:

- [PR #22](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/22) (deploy automático): o `Jenkinsfile` da branch ficou com uma `}` sobrando, o que quebraria a pipeline de todo mundo depois do merge (registrado na issue [#47](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/issues/47)). A branch foi corrigida e atualizada com a `main`, que já tinha o stage de Docker da #30. Também definimos que o deploy da `main` seria feito só pelo Jenkins, para o Render não publicar duas vezes.

- [PR #29](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/29): o GitHub descartava o merge devido a "merge-base changed", pois a branch que tentávamos integrar havia sido criada a partir de outra, mergeada primeiro. Quando isso aconteceu, a "base" deste PR mudou e o GitHub continuou com a referência antiga. Isso porque o repositório tem uma regra que descarta aprovações quando o código muda depois de aprovado, a fim de evitar a aprovação de uma versão e merge de outra. Dessa forma, a plataforma entendia que tudo havia sido alterado e descartaca as aprovações. Para solucionar a questão, a Karolina trouxe a `main` atual para sua própria branch e deu o comando `git push`. Assim, ambas as branches partiam da `main` mais recente e as aprovações foram aceitas.

- [PR #30](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/30) (build da imagem Docker no Jenkins): no Docker do WSL e do Linux, o Jenkins não tinha permissão para usar o Docker da máquina (`permission denied` no `/var/run/docker.sock`). A solução foi informar o grupo do Docker na variável `DOCKER_GID` do `.env`, o que ficou documentado no PR. No Docker Desktop, não é preciso configurar nada.

- [PR #59](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/59): com o merge de novas PRs na `main`, este PR apresentou conflitos de código e de cumprimento de requisitos. Para solucionar o problema, foram resolvidos os conflitos de código e, então, analisados os pontos levantados no comentário de avaliação da PR. Por fim, verificou-se a existência de outros pontos a serem considerados.

- [PR #68](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/68) e [PR #69](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/69): um **conflito semântico**. O Git juntou as duas sem acusar erro, mas a #68 trouxe o `podeGerenciarCatalogo` com o mesmo corpo que a #69 tinha acabado de centralizar no método `ehAdminAtivo`. O código compilava e os testes passavam, mas a duplicação voltou. A revisão percebeu, e um commit extra (`refactor(autenticacao): podeGerenciarCatalogo usa ehAdminAtivo`) fez o método também usar o `ehAdminAtivo`. A lição que tiramos é que merge sem conflito não garante código coerente.

## Lições aprendidas

### Lições do grupo

- **Organizar o trabalho desde o início:** quadro no GitHub Projects ligado a issues, com `Closes #N` nos PRs para fechar as issues automaticamente. Só passamos a usar as issues na reta final.
- **Acompanhar o meio de cada ciclo, não só o prazo:** em um ciclo houve apenas um PR, e no seguinte acumularam-se mais de quinze, o que concentrou revisões e conflitos nos últimos dias.
- **Criar as APIs mais cedo:** as telas ficaram dependendo de rotas que chegaram tarde e precisaram de dados mockados.
- **Não criar uma branch a partir da branch de outra pessoa:** isso gerou o problema de aprovações descartadas da #29.

### Beatriz

- **Lições aprendidas:** Aprendi muito, principalmente sobre o GitHub. Já tinha usado ele para versionamento, mas de forma muito básica. Foi a primeira vez que utilizei de fato essa diversidade de comandos (que aprendi durante a criação do projeto), a estrutura de branches, PRs com aprovação de outros colaboradores do projeto, até chegar ao merge. Também conheci o GitHub Projects ligado às issues. Fora do GitHub, pude aplicar na prática os testes que aprendemos em aula, em especial a utilização e a estruturação de mocks, e conhecer um pouco de novas ferramentas que usamos no projeto.

- **O que faria diferente?** Eu teria me planejado melhor em relação aos prazos e ao estudo das ferramentas e dos conceitos que eu não conhecia. Por ser um projeto bastante extenso, com um planejamento mais adequado seria possível estudar melhor, na prática, os conceitos que aprendemos em sala de aula, conhecer melhor as ferramentas e, talvez, até ampliar o escopo do projeto.

### Júlia

- **Lições aprendidas:** Nunca tinha utilizado as ferramentas que incorporamos nessa plataforma, então foi muito interessante entender como funcionavam, mesmo que algumas de forma mais superficial. Além disso, aprendi muito sobre GitHub, já que antes só o tinha utilizado de forma mais básica. Por fim, pude aplicar os conhecimentos que adquiri nas aulas de Engenharia de Software e entender melhor as etapas de produção e como tudo é feito.

- **O que faria diferente?** Teria me organizado melhor e, mesmo que não pudesse finalizar algo naquele momento, ao menos adiantaria o que fosse possível.

### Karolina

- **Lições aprendidas:** Nunca tinha feito um login de verdade, com senha protegida, token e permissões para cada perfil, então aprendi muito com o Supabase e o Spring Security. Também percebi que a minha parte afetava o trabalho de todas: quando a API passou a exigir login, os testes das colegas precisaram mudar, e tivemos que combinar juntas a ordem dos merges. Revisar as PRs das meninas me ajudou a entender o sistema inteiro, e não só o meu módulo. E aprendi a usar a IA com regras claras, decidindo eu mesma o que entrava no projeto e conferindo tudo antes de subir.

- **O que faria diferente?** Começaria direto com o Supabase, em vez de fazer primeiro um login provisório que depois tive que substituir, o que deu retrabalho. Também teria pesquisado mais cedo como o banco compartilhado funcionava, porque descobri o limite de conexões só quando ele travou durante os testes.

### Lilyan

 **Lições aprendidas:** Organizar a equipe é tão importante quanto escrever o código. Propus um quadro no Trello, que não teve adesão, e depois criei o quadro no GitHub Projects, mais próximo do repositório que todas já usavam. Também aprendi que deixar correções "para depois" vira uma bola de neve, visto que o contexto se perde e os problemas se acumulam. Por isso, passei a corrigir na hora até os detalhes menores. Revisar os PRs das colegas me fez entender os outros módulos e perceber problemas que os testes não pegam, como o conflito semântico entre a #68 e a #69. 
 
- **O que faria diferente?** Combinaria desde a primeira entrega o uso de issues com `Closes #N` e papéis definidos (quem prioriza e quem acompanha o andamento), em vez de deixar para a reta final. Também incluiria reuniões para demonstrar tudo que foi feito dentro de uma sprint, de modo que todas estejam no mesmo entendimento. 

### Yasmin

- **Lições aprendidas:**

- **O que faria diferente?**
