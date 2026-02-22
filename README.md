# 🍤 Coco Bambu | Gestão de Pedidos (Delivery API)

## 📖 Propósito do Sistema
Esta solução foi desenvolvida como resposta ao desafio técnico do Coco Bambu. O objetivo do sistema é atuar como o coração operacional de um restaurante de delivery, fornecendo uma API robusta para gerenciamento de pedidos e uma interface de usuário focada na orquestração de status em tempo real.

O sistema lê uma carga de dados inicial, permite a criação de novos pedidos e blinda a transição de status através de uma Máquina de Estados finita, garantindo a consistência do fluxo operacional (desde o recebimento até a entrega).

## 🚀 Tecnologias e Ecossistema

O projeto foi construído utilizando as seguintes tecnologias:

**Backend (API RESTful)**
* **Linguagem/Framework:** Java 21 e Spring Boot (v4.0.3)
* **Persistência:** Spring Data JPA / Hibernate
* **Banco de Dados:** MySQL 8.0
* **Testes:** JUnit 5 e Mockito

**Frontend (Interface Gráfica)**
* **Framework:** Angular 17+ (com Vite)
* **Estilização:** Bootstrap 5 e CSS customizado
* **Linguagem:** TypeScript e HTML5

**Infraestrutura e DevOps**
* **Conteinerização:** Docker e Docker Compose (Multi-stage builds)
* **Servidor Web:** Nginx (para o Frontend)

## 🏗️ Arquitetura e Design de Software

A solução foi desenvolvida seguindo os princípios do **SOLID** e utilizando a **Arquitetura em Camadas** (Controller, Service, Repository), garantindo uma clara separação de responsabilidades e facilitando a manutenibilidade.

* **Padrão DTO (Data Transfer Object):** O contrato de entrada/saída da API foi rigorosamente separado das entidades de banco de dados (`@Entity`). Isso protege os dados sensíveis, evita exposição excessiva do modelo de domínio e previne problemas de recursividade na serialização do JSON.
* **Validação de Integridade (Anti-fraude):** O `OrderService` recalcula o valor total baseado no preço e quantidade de cada item enviado, barrando a criação do pedido caso o `total_price` informado divirja da soma real dos itens.
* **Banco de Dados Relacional otimizado:** Em vez de normalizar excessivamente os dados do cliente e endereço em múltiplas tabelas (o que geraria *JOINs* pesados), utilizou-se o conceito de embutimento do JPA para persistir esses dados na própria tabela de pedidos, garantindo altíssima performance de leitura.

## 💡 Hipóteses Assumidas

Durante o desenvolvimento, algumas premissas arquiteturais e de negócio foram adotadas:

1. **Carga Inicial de Dados Segura:** O PDF exigia que o sistema considerasse os registros existentes no arquivo `pedidos.json`. Como hipótese de um sistema real e escalável, decidi **não** usar o arquivo `.json` como banco de dados em tempo real. Em vez disso, criei um `DataSeeder` em Java que lê este arquivo na inicialização e popula o banco de dados MySQL automaticamente caso ele esteja vazio.
2. **Exclusão Segura (Soft/Hard Delete):** Assumiu-se como regra estrita de negócio que um pedido **não pode ser deletado livremente**. Apenas pedidos que já atingiram o status `CANCELED` podem ser removidos fisicamente do banco de dados, garantindo rastreabilidade financeira e operacional.
3. **Interface Gráfica:** Como um diferencial, o Frontend foi desenhado com foco em telas Desktop/Tablets, simulando o painel de operação diária utilizado nas cozinhas e balcões de restaurantes de alto volume.

## 🐳 Como Executar a Solução

A aplicação foi totalmente conteinerizada (Docker) para garantir a portabilidade e facilidade de teste. **Não é necessário ter o Java, Node.js ou MySQL instalados na sua máquina.** O único pré-requisito é ter o [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução (garanta que não exista nenhuma programa utilizando as portas 3306 e 8080).

1. Faça o clone do repositório e acesse a pasta principal do projeto:
```bash
git clone https://github.com/GabrielKran/delivery-api

cd delivery-api
```
2. Suba a infraestrutura completa (Banco de Dados, Backend e Frontend) com um único comando:

```bash
docker-compose up --build
```
3. Aguarde o fim do processo de compilação. Quando o terminal indicar que os servidores iniciaram, abra o seu navegador e acesse à interface gráfica através do endereço:
👉 http://localhost:4200

Nota: Para encerrar a aplicação e limpar os dados temporários do banco de dados, utilize o comando abaixo no seu terminal aonde o programa está.

```bash
docker-compose down -v
```

## 🔀 Endpoints da API e Regras de Negócio

O backend expõe uma API RESTful completa na porta `8080` para o gerenciamento do fluxo de pedidos.

### Rotas Disponíveis (Controller)
* `GET /orders` : Retorna a lista de todos os pedidos de forma resumida (`OrderSummaryDTO`).
* `GET /orders/{id}` : Retorna os detalhes completos de um pedido específico.
* `POST /orders` : Cria um novo pedido no sistema.
* `PATCH /orders/{id}/status?newStatus={STATUS}` : Avança ou cancela o pedido atual.
* `DELETE /orders/{id}` : Exclui o pedido do sistema.

### 🛡️ Regras de Negócio Implementadas (Service)

O núcleo da aplicação (`OrderService`) contém validações estritas para garantir a consistência dos dados e da operação:

1. **Máquina de Estados Finita:** O fluxo de um pedido é unidirecional para garantir a consistência da operação na cozinha. 
   
   * Todo novo pedido nasce com o status `RECEIVED`.
   * **Transições Permitidas:**
     * `RECEIVED` ➔ `CONFIRMED` ou `CANCELED`
     * `CONFIRMED` ➔ `DISPATCHED` ou `CANCELED`
     * `DISPATCHED` ➔ `DELIVERED` ou `CANCELED`
   * **Bloqueios:** Não é possível retornar a um status anterior. Pedidos com status finais (`DELIVERED` ou `CANCELED`) são imutáveis e não aceitam novas transições.

2. **Regra de Exclusão:**
   A rota de `DELETE` possui uma trava de segurança. Apenas pedidos que estão no estado `CANCELED` podem ser apagados do banco de dados. Qualquer tentativa de apagar um pedido em andamento ou entregue retornará um erro.

3. **Validação Anti-fraude (Integridade de Preço):**
   Ao criar um pedido (`POST`), o sistema não confia cegamente no `total_price` enviado. Ele percorre a lista de itens, multiplica o `price` pela `quantity` de cada um e verifica se a soma bate com o valor total informado. Se houver divergência de centavos, a requisição é bloqueada.

## 📋 Backlog de Tarefas e Critérios de Aceitação

Para o desenvolvimento desta solução, o escopo do teste técnico foi quebrado em entregáveis menores, simulando um planejamento de *Sprint* real (Task Board). Abaixo está o backlog das tarefas executadas e seus respectivos critérios de aceitação.

### 📝 Tarefa 1: Modelagem de Dados e Carga Inicial (Seeder)
* **Descrição:** Configurar as entidades JPA e garantir que o sistema inicie com os dados do arquivo `pedidos.json` fornecido.
* **Critérios de Aceitação:**
  * As entidades devem usar `@Embeddable` para dados de cliente e endereço para otimizar leitura.
  * O sistema não deve depender do arquivo físico em tempo de execução contínua.
  * O `DataSeeder` deve ler o `pedidos.json` ao iniciar o Spring Boot e popular o banco de dados relacional apenas se o banco estiver vazio.

### 📝 Tarefa 2: Desenvolvimento da API RESTful (CRUD)
* **Descrição:** Criar os endpoints de listagem, criação e deleção de pedidos isolando a camada de banco da camada web.
* **Critérios de Aceitação:**
  * `GET /orders` deve retornar uma lista resumida (DTO leve).
  * `GET /orders/{id}` deve retornar o JSON completo com os arrays de itens, pagamentos e histórico.
  * `POST /orders` deve calcular o `total_price` internamente validando contra fraudes.
  * `DELETE /orders/{id}` só pode permitir a exclusão de pedidos se o `last_status_name` for igual a `CANCELED`.

### 📝 Tarefa 3: Implementação da Máquina de Estados
* **Descrição:** Criar a regra de negócio rigorosa que controla o avanço do status dos pedidos na cozinha.
* **Critérios de Aceitação:**
  * Todo novo pedido deve nascer obrigatoriamente como `RECEIVED`.
  * O status só pode avançar na seguinte ordem: `RECEIVED` ➔ `CONFIRMED` ➔ `DISPATCHED` ➔ `DELIVERED`.
  * O pedido pode ser movido para `CANCELED` a partir de qualquer status, exceto se já estiver finalizado.
  * Retornos de status (ex: `CONFIRMED` para `RECEIVED`) devem lançar erro de transição inválida.

### 📝 Tarefa 4: Construção do Frontend (Dashboard)
* **Descrição:** Desenvolver uma interface visual para os gerentes visualizarem os pedidos e interagirem com a Máquina de Estados.
* **Critérios de Aceitação:**
  * Tela principal com listagem de pedidos e botões de filtro rápido por status.
  * Modal/Side-sheet para exibir os detalhes do pedido e conter os botões de avanço de status ou cancelamento.
  * Formulário dinâmico para criação de novos pedidos.
  * Comunicação via chamadas HTTP (RxJS/Observables) tratadas corretamente.

### 📝 Tarefa 5: Conteinerização e DevOps
* **Descrição:** Empacotar a aplicação utilizando Docker para garantir que o avaliador consiga rodar o projeto sem configurar ambiente local.
* **Critérios de Aceitação:**
  * Criar `Dockerfile` multi-stage para o Spring Boot (Java 21).
  * Criar `Dockerfile` multi-stage para o Angular (Node + Nginx).
  * Criar `docker-compose.yml` conectando as duas aplicações a um container do MySQL 8.0, populando as variáveis de ambiente necessárias.

### 🚀 Próximos Passos (Evoluções Futuras)
Caso o projeto fosse continuado para novas Sprints, o backlog receberia as seguintes tarefas:
* **Autenticação:** Implementação de Spring Security + JWT para acesso restrito.
* **Testes no Frontend:** Criação de suíte de testes com Jest/Jasmine para os componentes Angular.
* **Tempo Real:** Substituição do botão "Atualizar Dados" por WebSockets (ou SSE) para os pedidos atualizarem na tela automaticamente.