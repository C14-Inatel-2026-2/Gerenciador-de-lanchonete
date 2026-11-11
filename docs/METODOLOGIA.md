# Metodologia de desenvolvimento

>Este documento descreve como foi organizado o trabalho ao longo do semestre, incluindo metodologia, por quê, papéis, cadência, ferramentas, DoD/DoR e métricas.
> O relato do dia a dia (conflitos, bloqueios e lições) está em [`DINAMICA.md`](DINAMICA.md).

## 1. Qual metodologia usamos (e por quê)

Usamos um **Kanban leve com ciclos fixos de 2 semanas**. Não seguimos Scrum formal.

- **Os ciclos vinham da disciplina.** O monitor pedia uma entrega a cada 2 semanas, então cada entrega funcionou como uma "sprint" com data fixa. Sendo assim, não precisamos definir a duração do ciclo.
- **O trabalho fluía de forma contínua dentro do ciclo.** Cada integrante puxava as tarefas do seu módulo, abria uma PR e passava pela revisão. Não havia planning, daily nem review formais.
- **Por que não Scrum:** chegamos a discutir definir um PO e adotar Scrum, mas a proposta não foi adiante. Com horários diferentes e comunicação quase toda assíncrona (WhatsApp), as reuniões e regras do Scrum não se encaixaram na rotina do grupo. O que se manteve foi o que funcionava de forma assíncrona: **issues, PRs com revisão obrigatória e pipeline**.

## 2. Papéis

**Não definimos papéis formais.** Os papéis abaixo surgiram na prática:

| Papel na prática | Quem | Evidência |
|---|---|---|
| Liderança | Júlia no início; depois Beatriz | definido no início do projeto |
| Envio das entregas ao monitor | Beatriz | entregas pelo Teams |
| Planejamento das tarefas | cada integrante no seu módulo; no ciclo final, Karolina consolidou o plano | issues `entrega-final` (#35 a #56), com contexto, tarefas, testes e prazo |
| Histórias de usuário | cada integrante escreveu as do seu módulo | US01 e US07 (Karolina), US02 e US06 (Beatriz), US03 (Júlia), US04 (Lilyan), US05 (Yasmin) |
| Estrutura do CI/CD | Lilyan | Jenkinsfile e Jenkins como código (#12), versões fixadas (#18); cada integrante comitou o seu próprio stage |
| Administração do repositório | Lilyan | regra de proteção da `main` e quadro no GitHub Projects |
| Deploy (Render e Vercel) | Yasmin | #17, #22 |
| Revisão de PRs | todas | toda PR precisa de 1 aprovação de outra integrante |
| Desenvolvedoras | todas | cada uma com um módulo de domínio, fazendo backend, frontend e testes do próprio módulo |

**Divisão por módulo, sem "dona do frontend" ou "dona do backend":** autenticação (Karolina), catálogo (Beatriz), carrinho e pedidos (Júlia), ciclo de vida do pedido e cozinha (Lilyan), painel admin e deploy (Yasmin). Assim todas passaram por todas as camadas.

## 3. Cadência

| Ciclo | Período | Entrega |
|---|---|---|
| C0 | 07/08 a 21/08 | 1 — ideia e formação do grupo |
| C1 | 28/08 a 11/09 | 2 — desenvolvimento inicial do projeto |
| C2 | 11/09 a 02/10 | 3 — testes unitários |
| C3 | 03/10 a 17/10 | 4 — pipeline de CI/CD |
| C4 | 17/10 a 31/10 | 5 — agile e engenharia de requisitos |
| C5 | 03/11 a 13/11 | 6 — refactoring e entrega final |

- **Reuniões:** uma reunião, antes da entrega 1, para definir a ideia. Depois disso, todas as decisões foram tomadas no grupo de WhatsApp.
- **Revisão do que foi feito:** acontecia nas PRs, com comentários e pedidos de mudança, e em mensagens no WhatsApp, e não em reuniões.

## 4. Ferramentas

| Ferramenta | Uso |
|---|---|
| WhatsApp | comunicação principal e decisões do grupo |
| Microsoft Teams | envio das entregas ao monitor |
| GitHub (repositório, branches, PRs) | código, revisão obrigatória antes do merge na `main` |
| GitHub Issues | histórias de usuário (US01 a US07) e tarefas do ciclo final, com dono e prazo |
| GitHub Projects | quadro Kanban (A fazer, Em andamento, Em revisão, Concluído) — ver seção 7 |
| Trello | primeira tentativa de quadro, abandonada (ver seção 7) |
| Jenkins | pipeline de CI/CD com build, testes, relatórios, imagem Docker e deploy |

## 5. Definição de Preparado (DoR)

Formalizada só no ciclo final, nas issues `entrega-final`. Uma tarefa estava pronta para começar quando tinha:
- **contexto** (qual história ou problema ela atende);
- **o que fazer** (backend, frontend e as rotas da API combinadas no contrato `docs/api.md`, que diz, para cada rota, o método, quem pode acessar, o que enviar e o que ela responde);
- **como testar** (quais testes automatizados escrever);
- **como conferir** (critérios de aceitação);
- **dono e prazo**.

## 6. Definição de Pronto (DoD)

Parte da definição de pronto é **garantida por ferramenta**, e parte foi **combinada entre o grupo**:

**Garantido por ferramenta**
1. **Toda mudança entra por PR.** A `main` é protegida.
2. **Pelo menos 1 aprovação** de outra integrante, pela regra de proteção da `main`.
3. **Pipeline do Jenkins verde** (build, testes do backend e do frontend, lint, imagem Docker e build do front), desde que a pipeline foi criada. Na `main`, o **deploy** (Render e Vercel) só roda depois de todos os stages passarem.

**Combinado entre o grupo**
4. **Testes automatizados do comportamento novo.** A partir da entrega 3 (testes unitários), todas as PRs que mudaram código trouxeram testes. As PRs sem teste são do início do projeto (#3, #4 e #5) e uma de configuração do Docker (#15).
5. **Contrato da API (`docs/api.md`) atualizado** quando uma rota é criada ou muda. O contrato existe desde a #57 (05/11) e foi atualizado nas PRs que mudaram rotas (#57, #58, #63 e #68).

## 7. Métricas

Os dados abaixo foram tirados do histórico do Git da `main`.

### PRs mergeadas por ciclo

| Ciclo | PRs | Quais |
|---|---|---|
| C1 | 5 | #1 a #5 |
| C2 | 5 | #6, #7, #8, #10, #11 |
| C3 | 6 | #9, #12, #13, #14, #15, #17 |
| C4 | **1** | #26 |
| C5 | **15** | #18, #21, #22, #29, #30, #32, #33, #57, #58, #60, #61, #62, #63, #66, #68 |
| C6 | preencher depois que finalizar tudo|

### Lead time (do primeiro commit da PR até o merge)

- **Mediana geral:** 1,2 dia (32 PRs).
- **PRs #1 a #17:** mediana de 0,35 dia. **PRs a partir da #18:** mediana de 3,25 dias.

### PRs com testes

- Das **18 PRs que mudaram código**, **14 trouxeram testes automatizados**.

### O que as métricas mostram

- **O ciclo C4 quase não teve entregas na `main`** (1 PR), e o trabalho se acumulou no ciclo final. 
- **O lead time aumentou de ~8 horas para ~3 dias** a partir da #18. Isso coincide com a regra de 1 aprovação e com revisões que pediam mudanças. As PRs passaram a demorar mais porque a revisão ficou mais trabalhosa.
- **O quadro não serviu como fonte de métricas.** O Trello não teve adesão. No GitHub Projects, os cards de rascunho não se moviam sozinhos e o quadro ficou desatualizado. Por isso, as métricas vêm do histórico do Git e das PRs, e não do quadro.

## 8. O que faríamos diferente

**Antes do primeiro código**
- Fazer um **mockup das telas**, para todas saberem o que construir no frontend.
- Fazer **diagramas do projeto do início ao fim** e definir uma estrutura mais detalhada, já que a inicial ficou genérica demais.
- Criar o **GitHub Projects logo no começo e transformar tudo em issue**, para o quadro andar sozinho com as PRs (`Closes #N` + automação).

**Organização do grupo**
- Definir **responsáveis por frente** (frontend, backend, DevOps), mas com todas contribuindo em todas as áreas, para qualquer uma poder ajudar onde fosse preciso.
- Ter uma **líder** acompanhando o repositório e o quadro e cobrando os prazos.
- Fazer **uma reunião a cada 2 semanas, um dia antes da entrega**, para garantir que tudo estivesse mergeado antes da data limite.

**Durante o projeto**
- Manter a **documentação sempre atualizada** e conferir se tudo entrou no quadro.
- Sempre explicar **o que está acontecendo, de onde estamos saindo e para onde estamos indo**.
- Não deixar acumular, o trabalho precisa andar dentro do ciclo, e não só perto da entrega.

---