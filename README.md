# 💳 Oficina Billing Service

## Autor
- **Nome:** Lucas Eduardo Peters Scuissiatto
- **RM:** RM365455
- **Curso:** Pós-Graduação em Arquitetura de Software

---

## 📖 Descrição

O **Billing Service** é responsável pela cobrança das Ordens de Serviço. Ele recebe as OS com orçamento fechado, gera o link de pagamento no **Mercado Pago** e, quando o Mercado Pago confirma ou recusa o pagamento via webhook, publica o resultado para os demais serviços da saga.

## 🚀 Tecnologias
* **Java 17 / Spring Boot 3**
* **Spring Data JPA + MySQL**: registro das cobranças.
* **RabbitMQ**: comunicação orientada a eventos com o OS Service e o Execution Service.
* **SDK do Mercado Pago**: criação de preferências de pagamento e consulta de pagamentos.
* **JUnit 5 & Mockito**, **JaCoCo** e **SonarCloud**.

## 🏗️ Fluxo

1. **Cobrança**: consome `os-created-queue` (ID e valor da OS), cria a preferência no Mercado Pago, salva a cobrança como `PENDING` e publica o link em `payment-link-generated-queue`. O OS Service envia esse link por e-mail ao cliente.
2. **Confirmação**: o Mercado Pago chama `POST /billing/webhook`. O serviço consulta o pagamento na API e publica:
   * aprovado → `os-payment-approved-queue` e `execution-payment-approved-queue`;
   * recusado ou cancelado → `payment-refused-queue` (compensação da saga).

## 📥 Fila de Entrada (Consumer)
* `os-created-queue`

## 📤 Filas de Saída (Producer)
* `payment-link-generated-queue`
* `os-payment-approved-queue`
* `execution-payment-approved-queue`
* `payment-refused-queue`

## 🛡️ Resiliência
* **Retry com espera crescente**: 3 tentativas (1s, 2s) antes de desistir de uma mensagem.
* **DLQ**: mensagens que falham em todas as tentativas vão para `oficina-billing-service.dlq`, com o motivo da falha nos headers (`x-exception-message`, `x-exception-stacktrace`).
* **Idempotência**: se a mesma OS chegar duas vezes (reentrega), não é criada uma segunda cobrança.

## ⚙️ Configuração

Nenhum segredo fica no código. Variáveis de ambiente (veja `.env.example`):

| Variável | Descrição |
| --- | --- |
| `MERCADOPAGO_ACCESS_TOKEN` | Token do Mercado Pago. Use um token de **teste** (`TEST-...`) em desenvolvimento. |
| `MERCADOPAGO_NOTIFICATION_URL` | URL pública do serviço, usada pelo webhook (ex.: túnel do ngrok). |
| `DB_PASSWORD` | Senha do MySQL (padrão local: a do docker-compose da infra). |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ (padrão local: a do docker-compose da infra). |

No Kubernetes, os valores vêm do Secret `oficina-secrets` (modelo em `oficina-infra-k8s/secrets.example.yaml`).

## 🐳 Como rodar localmente

1. Suba as dependências (MySQL, RabbitMQ etc.) com o docker-compose do repositório [`oficina-infra-k8s`](https://github.com/oficina-mecanica-app/oficina-infra-k8s).
2. Defina `MERCADOPAGO_ACCESS_TOKEN` e `MERCADOPAGO_NOTIFICATION_URL`.
3. Execute:

```bash
./mvnw spring-boot:run
```

Para simular uma recusa de pagamento sem passar pelo Mercado Pago:

```bash
curl -X POST http://localhost:8081/billing/test/refuse/1
```

## 🧪 Testes

```bash
./mvnw clean verify   # testes + relatório de cobertura (JaCoCo)
```

A pipeline do GitHub Actions roda os testes, envia a análise para o SonarCloud e publica a imagem Docker a cada push na `main`.
