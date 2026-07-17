# Decisoes Tecnicas e Trade-offs

Este projeto foi desenvolvido como uma aplicacao full stack para gestao de pedidos, priorizando simplicidade, organizacao por camadas e atendimento aos requisitos do desafio.

## Backend

O backend foi construido com Java e Spring Boot por ser uma stack madura para APIs REST, com bom suporte a seguranca, validacao, persistencia, testes e observabilidade.

A aplicacao foi separada em camadas:

- `Controller`: exposicao dos endpoints REST.
- `Service`: regras de negocio, como limite de pedidos, status inicial e transicoes de status.
- `Repository`: acesso a dados com Spring Data JPA.
- `Mapper/DTO`: separacao entre entidade interna e resposta da API.

O uso de DTOs evita expor diretamente a entidade JPA e facilita controlar o contrato da API. Um exemplo disso foi o campo `totalWeight`, calculado no backend para manter a regra de negocio centralizada.

## Banco de Dados

Foi utilizado H2 em memoria para facilitar a execucao local e a avaliacao do desafio. O trade-off e que os dados nao sao persistidos apos reiniciar a aplicacao. Para producao, o mais adequado seria um banco relacional persistente, como PostgreSQL.

## Autenticacao

A autenticacao foi feita com JWT. Essa escolha deixa o backend stateless, facilita a protecao das rotas e permite que o frontend envie o token nas chamadas protegidas.

O trade-off e que o token precisa ser armazenado no navegador. Para este desafio, foi mantida uma abordagem simples. Em um ambiente produtivo, eu avaliaria estrategias mais robustas, como cookies `HttpOnly`, expiracao curta e refresh token.

## Frontend

O frontend foi desenvolvido com Angular e Angular Material. A aplicacao foi organizada com paginas, services, models, guards e interceptor HTTP.

- `Guards`: protegem rotas autenticadas e evitam acesso indevido ao login quando o usuario ja esta logado.
- `Interceptor`: adiciona o token JWT nas requisicoes para a API.
- `Services`: concentram a comunicacao com o backend.
- `Components`: cuidam da interface e apresentacao dos dados.

Os graficos do dashboard foram feitos com HTML/CSS e Angular, sem biblioteca externa de graficos. A decisao reduz dependencias e atende bem ao escopo visual do desafio, mas uma biblioteca como Chart.js poderia ser avaliada se os graficos ficassem mais complexos.

## Observabilidade

Foram habilitados Actuator, Micrometer, Prometheus e Grafana para acompanhar saude, metricas tecnicas e metricas de negocio. Tambem foram adicionados logs estruturados no backend com niveis `INFO`, `WARN` e `ERROR`.

O trade-off foi manter a observabilidade simples e local via Docker Compose, suficiente para demonstrar o conceito sem aumentar demais a complexidade do projeto.

## Docker

O Docker Compose foi usado para facilitar a execucao do ambiente completo: backend, frontend, Prometheus e Grafana. O frontend e servido por Nginx porque, apos o build, o Angular vira arquivos estaticos.

Essa abordagem deixa o projeto mais proximo de um ambiente real do que usar `ng serve` como servidor final.