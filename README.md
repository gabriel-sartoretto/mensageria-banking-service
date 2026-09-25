# banking-service

Microsserviço de **cadastro de agências**. Valida a agência no banking-validation antes de salvar e consome do Kafka
os pedidos de remoção de agências inativadas, fechando a saga correspondente.

Faz parte do projeto [kafka-rabbitmq-banking](https://github.com/gabriel-sartoretto/kafka-rabbitmq-banking), junto com
[banking-validation](https://github.com/gabriel-sartoretto/mensageria-banking-validation) e
[banking-audit](https://github.com/gabriel-sartoretto/mensageria-banking-audit).

**Stack:** Java 21 · Quarkus 3.15 · Hibernate Reactive Panache · PostgreSQL · REST Client · Kafka + Avro/Schema Registry · Micrometer/Prometheus

## O que ele faz

### Cadastro (`POST /agencia`)
1. Consulta `GET /situacao-cadastral/{cnpj}` no banking-validation.
2. Recusa se a agência não existir lá ou não estiver `ATIVO` (`AgenciaNaoAtivaOuNaoEncontradaException`).
3. Recusa se o CNPJ já estiver cadastrado aqui (`AgenciaJaExistenteException`).
4. Caso contrário, persiste a agência.

### Remoção via Kafka (`RemoverAgenciaService`)
Consome o tópico `remover-agencia-avro` (grupo `banking-service-consumer-group`), com mensagens Avro `br.com.alura.Agencia`
enviadas pelo banking-validation quando uma agência fica INATIVO. Para cada mensagem:

| Situação | Ação | Fechamento da saga no validation |
|---|---|---|
| Agência encontrada | Remove do banco | `PUT /saga/sucesso` |
| Agência não existe mais (ex.: reenvio do resync) | Nada | `PUT /saga/ignorada` |
| Nome da agência contém `ERRO` | **Falha simulada**, para testar o fluxo de erro | `PUT /saga/erro` |
| Falha real na remoção | — | `PUT /saga/erro` |

Mensagens antigas, sem `sagaId`, são processadas mas não fecham saga.

## Endpoints (porta 8080)

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/agencia` | Cadastra uma agência (validada no banking-validation) |
| `GET` | `/metrics` | Métricas Prometheus |

```bash
curl -X POST localhost:8080/agencia -H "Content-Type: application/json" \
  -d '{"nome":"Agencia BSB","razaoSocial":"Asa Norte AGENCIA BSB","cnpj":"15130254000100","situacaoCadastral":"ATIVO"}'
```

## Rodando localmente

Pré-requisito: a infraestrutura (Kafka, Schema Registry etc.) e a API do
[banking-validation](https://github.com/gabriel-sartoretto/mensageria-banking-validation) rodando (porta 8181).

```bash
docker compose up -d postgres-db-alura-banking-service   # PostgreSQL na porta 5433, banco "agencia"
./mvnw quarkus:dev
```

O `docker-compose.yml` também tem o serviço `banking-service` (imagem `joao0212/banking-service:v1`, do curso), caso
queira rodar a API pelo Docker em vez do `quarkus:dev`.

Variáveis de ambiente (com os valores padrão):

| Variável | Padrão |
|---|---|
| `QUARKUS_DATASOURCE_HOST` / `_PORT` | `localhost` / `5433` |
| `QUARKUS_DATASOURCE_USERNAME` / `_PASSWORD` | `joao` / `joao` |
| `QUARKUS_CLIENT_HTTP` / `_PORT` (banking-validation) | `localhost` / `8181` |
| `QUARKUS_KAFKA_HOST` / `_PORT` | `localhost` / `9092` |

## Schema Avro

`src/main/avro/Agencia.avsc` gera a classe `br.com.alura.Agencia`. O mesmo arquivo existe no banking-validation:
qualquer mudança precisa ser feita **nos dois** e manter compatibilidade (novos campos com `default`, como `sagaId`).

## Build

```bash
./mvnw package                                  # target/quarkus-app/quarkus-run.jar
./mvnw package -Dquarkus.package.jar.type=uber-jar
./mvnw package -Dnative                         # executável nativo (requer GraalVM)
```
