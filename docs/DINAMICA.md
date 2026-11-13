# Dinâmica de desenvolvimento do grupo

## Divisão de tarefas

| Integrante | Módulo desenvolvido     |
|------------|-------------------------|
| Beatriz    | Catálogo & Produtos     |
| Júlia      | Carrinho & Pedidos      |
| Karolina   | Autenticação & Usuários |
| Lilyan     | Ciclo de Vida do Pedido |
| Yasmin     | Infraestrutura          |

A divisão foi feita considerando-se o **equilíbrio de tarefas**, no intuito de evitar que alguma integrante ficasse sobrecarregada.

## Fluxo de branches

As branches utilizadas apresentavam marcadores em sua nomenclatura, como `feature/`, `fix/`, `ci/`, `docs/` e `refactor/`, com o objetivo de explicitar o que estaria sendo executado.

Após as mudanças, eram feitos Pull Requests (PRs) para solicitar a integração das mudanças à branch Main, que ficou protegida de quebras.

## Padrão de commits

O padrão adotado foi **Conventional Commits**, com a utilização de marcações como `feat`, `fix`, `test`, `ci`, `docs` e `refactor`, cujo objetivo é criar um histórico de alterações claro, organizado e legível.

Commits realizados incluem:

- ci: adiciona stage de testes com relatório JUnit e cobertura JaCoCo
- test(compras): adiciona testes unitários para modelos e serviços
- refactor(autenticacao): podeGerenciarCatalogo usa ehAdminAtivo
- docs: seção Uso de IA no README
- feat(frontend): inicializa aplicacao React

## Code review

A fim de reduzir a integração de códigos e estruturas que não estivessem alinhados ao planejamento da plataforma, adotamos como regra a exigência de, pelo menos, uma aprovação em cada PR. Essa prática permitiu identificar e corrigir problemas durante o desenvolvimento, além de promover melhorias no código a partir das revisões realizadas. A título de exemplo, cita-se:

- Restauração de comentários: [PR #18](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/18) e [PR #21](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/21)
- Ajuste de Requisitos Não-Funcionais (RNFs): [PR #29](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/29)

## Conflitos e bloqueios

Durante o desenvolvimento, ocorreram casos em que os arquivos presentes no PR conflitavam com os presentes na Main, que havia sido atualizada. Com isso, foi necessário encontrar maneiras de resolvê-los, como nessas PRs:

- [PR #22](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/22):

- [PR #29](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/29): o GitHub descartava o merge devido a "merge-base changed", pois a branch que tentávamos integrar havia sido criada a partir de outra, mergeada primeiro. Quando isso aconteceu, a "base" deste PR mudou e o GitHub continuou com a referência antiga. Isso porque o repositório tem uma regra que descarta aprovações quando o código muda depois de aprovado, a fim de evitar a aprovação de uma versão e merge de outra. Dessa forma, a plataforma entendia que tudo havia sido alterado e descartaca as aprovações. Para solucionar a questão, a Karolina trouxe a main atual para sua própria branch e deu o comando `git push`. Assim, ambas as branches partiam da main mais recente e as aprovações foram aceitas.

Karol
- [PR #30](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/30): 

- [PR #59](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/59): com o merge de novas PRs à main, este PR apresentou conflitos de código e de cumprimento de requisitos. Para solucionar o problema, foram resolvidos os conflitos de código e, então, analisados os pontos levantados no comentário de avaliação da PR. Por fim, verificou-se a existência de outros pontos a serem considerados.

- [PR #68](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/68) e [PR #69](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/69):

- [PR #80](https://github.com/C14-Inatel-2026-2/Gerenciador-de-lanchonete/pull/80):

## Lições aprendidas

### Beatriz

- **Lições aprendidas:**

- **O que faria diferente?**

### Júlia

- **Lições aprendidas:** Nunca tinha utilizado as ferramentas que incorporamos nessa plataforma, então foi muito interessante entender como funcionavam, mesmo que algumas de forma mais superficial. Além disso, aprendi muito sobre GitHub, já que antes só o tinha utilizado de forma mais básica. Por fim, pude aplicar os conhecimentos que adquiri nas aulas de Engenharia de Software e entender melhor as etapas de produção e como tudo é feito.

- **O que faria diferente?** Teria me organizado melhor e, mesmo que não pudesse finalizar algo naquele momento, ao menos adiantaria o que fosse possível.

### Karolina

- **Lições aprendidas:**

- **O que faria diferente?**

### Lilyan

- **Lições aprendidas:**

- **O que faria diferente?**

### Yasmin

- **Lições aprendidas:**

- **O que faria diferente?**