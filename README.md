# Oficina Billing Service

## Autor
- **Nome:** Lucas Eduardo Peters Scuissiatto
- **RM:** RM365455
- **Curso:** Pós-Graduação em Arquitetura de Software

---

## 📖 Descrição

Esta é a segunda fase do projeto de backend para um Sistema Integrado de Atendimento para uma oficina mecânica. A solução inicial, desenvolvida na Fase 1, foi evoluída para garantir **qualidade, resiliência e escalabilidade**, incorporando práticas modernas de DevOps e infraestrutura.

O objetivo desta fase foi transformar a aplicação monolítica em um sistema robusto, preparado para alta disponibilidade e automação. Para isso, o código foi refatorado para seguir os princípios da **Clean Architecture**, a infraestrutura foi provisionada como código (**IaC**) com **Terraform**, a orquestração de contêineres é gerenciada pelo **Kubernetes**, e uma pipeline de **CI/CD** com **GitHub Actions** automatiza os processos de teste, build e publicação de imagens.

## ✨ Funcionalidades

O sistema mantém as funcionalidades do MVP da Fase 1, agora sustentadas por uma arquitetura mais avançada:

* [x] **Gestão Completa (CRUD):** Clientes, Veículos, Serviços e Peças.
* [x] **Criação e Gestão de Ordens de Serviço (OS):** Ciclo de vida completo, do recebimento à entrega.
* [x] **Consulta de Status:** Endpoint público para clientes.
* [x] **Notificações por Email:** Orçamentos enviados via MailHog.
* [x] **Análise e Monitoramento:** Cálculo de tempo médio de serviços.
* [x] **Segurança:** Autenticação via API com JWT.

## 🏗️ Arquitetura da Solução e Tecnologias

A arquitetura foi redesenhada para suportar escalabilidade e automação.

### Componentes da Arquitetura

* **Clean Architecture:** O código-fonte foi reestruturado para garantir baixo acoplamento e alta testabilidade, com as seguintes camadas principais:
    * **`domain`**: Contém as regras de negócio e entidades puras.
    * **`application`**: Orquestra os casos de uso e a lógica da aplicação.
    * **`presentation`**: A camada de entrada da API, responsável por expor os endpoints REST (Controllers) e tratar os erros de requisição (Handlers).
    * **`infrastructure`**: Implementa os detalhes técnicos e a comunicação com o mundo exterior, como o acesso ao banco de dados (`persistence`), o envio de e-mails (`service`) e a configuração de segurança (`security`).
* **Infraestrutura como Código (IaC) & Orquestração:**
    * **Terraform:** Utilizado para provisionar o cluster Kubernetes local de forma automatizada. Os scripts estão na pasta `/infra`.
    * **Kubernetes (Kind):** O cluster é orquestrado pelo Kubernetes, rodando localmente via Kind (Kubernetes in Docker).
    * **Manifestos K8s:** A implantação da aplicação e de suas dependências (PostgreSQL, MailHog) é descrita em ficheiros YAML na pasta `/k8s`, contemplando `Deployments`, `Services`, `ConfigMaps`, `Secrets` e `HPA` para auto-scaling.

* **CI/CD (Integração e Entrega Contínua):**
    * **GitHub Actions:** Uma pipeline automatizada é acionada a cada `push` na branch `main`.
    * **Fluxo:** A pipeline executa os testes unitários, constrói a imagem Docker da aplicação e a publica no **GitHub Container Registry (GHCR)**, pronta para ser implantada.

### Tecnologias Utilizadas
* **Linguagem:** Java 17+
* **Framework Principal:** Spring Boot 3.x
* **Segurança:** Spring Security com JWT
* **Banco de Dados:** PostgreSQL (em contêiner)
* **Infraestrutura como Código:** Terraform
* **Orquestração:** Kubernetes (Kind)
* **Conteinerização:** Docker
* **CI/CD:** GitHub Actions
* **Testes:** JUnit 5, Mockito
* **Documentação da API:** SpringDoc (Swagger)
* **Servidor de Email (Dev):** MailHog

---

## 🚀 Guia Rápido de Execução (5 minutos)

Escolha uma das três formas de executar o projeto conforme sua necessidade:

### 🐳 Caminho 1: Docker Compose (Mais Rápido)

Ideal para desenvolvimento rápido e testes locais.

**Pré-requisitos:** Docker

**Comandos:**
```bash
git clone <URL_DO_REPOSITORIO>
cd oficina
docker compose up --build
```

**Acesso:**
- API: `http://localhost:8080/swagger-ui/index.html`
- MailHog: `http://localhost:8025`

---

### ☸️ Caminho 2: Kubernetes com Terraform (Produção-like)

Ambiente completo com orquestração Kubernetes e provisionamento via Terraform.

**Pré-requisitos:** Docker, Terraform, Kind, kubectl

**Comandos:**
```bash
# 1. Provisionar cluster e banco de dados
    cd infra
    terraform init
terraform apply -auto-approve

# 2. Construir e carregar imagem
    cd ..
    docker build -t oficina-app:latest .
kind load docker-image oficina-app:latest --name oficina-cluster

# 3. Deploy da aplicação
kubectl apply -f k8s/

# 4. Verificar status
kubectl get pods
```

**Acesso:**
- API: `http://localhost:8081/swagger-ui/index.html`
- MailHog: `kubectl port-forward svc/mailhog-service 8025:8025` (em outro terminal)

**Nota:** O módulo Terraform de banco de dados (`infra/modules/database`) provisiona o PostgreSQL com state próprio, variáveis e outputs que alimentam o deploy da API.

---

### 💻 Caminho 3: Execução Local

Para desenvolvimento sem containers.

**Pré-requisitos:** Java 17+, Maven, PostgreSQL rodando localmente

**Comandos:**
```bash
# 1. Configurar banco de dados local
createdb oficina_db
# ou via psql: CREATE DATABASE oficina_db;

# 2. Configurar variáveis de ambiente (veja tabela abaixo)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/oficina_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=sua_senha
export JWT_SECRET=sua-chave-secreta-jwt
export SPRING_MAIL_HOST=localhost
export SPRING_MAIL_PORT=1025

# 3. Executar aplicação
./mvnw spring-boot:run
```

**Acesso:**
- API: `http://localhost:8080/swagger-ui/index.html`

---

## 📋 Variáveis de Ambiente

| Variável | Descrição | Valor Padrão | Exemplo |
|----------|-----------|--------------|---------|
| `SPRING_DATASOURCE_URL` | URL de conexão JDBC com o banco PostgreSQL | `jdbc:postgresql://localhost:5432/oficina_db` | `jdbc:postgresql://postgres-service:5432/oficina_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco de dados | `postgres` | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco de dados | - | `senhaSuperSecreta` |
| `JWT_SECRET` | Chave secreta para assinatura de tokens JWT | `a-chave-secreta-para-a-sua-api-deve-ser-bem-longa-e-segura` | `minha-chave-super-secreta-jwt-2024` |
| `SPRING_MAIL_HOST` | Host do servidor de email (SMTP) | `localhost` | `mailhog-service` |
| `SPRING_MAIL_PORT` | Porta do servidor de email | `1025` | `1025` |
| `APP_ADMIN_DEFAULT_PASSWORD` | Senha padrão do usuário admin criado na inicialização | `password` | `admin123` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estratégia de criação/atualização do schema | `update` | `update`, `create`, `none` |
| `APP_DATA_INITIALIZE` | Habilita inicialização de dados de exemplo | `true` | `true`, `false` |

---

## 📖 Roteiro de Chamadas - Fluxo Ponta a Ponta

Este roteiro demonstra o fluxo completo do sistema, desde a autenticação até a aprovação/recusa de orçamento.

### 1. Autenticação

```bash
# POST /login
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'

# Resposta: { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

**Guarde o token retornado para usar nos próximos passos.**

### 2. Criar Cliente

```bash
# POST /clients
curl -X POST http://localhost:8080/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "name": "João Silva",
    "document": "12345678900",
    "email": "joao@example.com",
    "phone": "11999999999"
  }'

# Resposta: { "id": 1, "name": "João Silva", ... }
```

### 3. Criar Veículo

```bash
# POST /vehicles
curl -X POST http://localhost:8080/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "clientId": 1,
    "brand": "Toyota",
    "model": "Corolla",
    "year": 2020,
    "plate": "ABC1234"
  }'

# Resposta: { "id": 1, "plate": "ABC1234", ... }
```

### 4. Criar Serviço

```bash
# POST /services
curl -X POST http://localhost:8080/services \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "name": "Troca de Óleo",
    "description": "Troca completa de óleo e filtro",
    "price": 150.00
  }'

# Resposta: { "id": 1, "name": "Troca de Óleo", ... }
```

### 5. Criar Peça

```bash
# POST /parts
curl -X POST http://localhost:8080/parts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "name": "Filtro de Óleo",
    "description": "Filtro de óleo original",
    "price": 45.00,
    "stockQuantity": 10
  }'

# Resposta: { "id": 1, "name": "Filtro de Óleo", ... }
```

### 6. Criar Ordem de Serviço

```bash
# POST /service-orders
curl -X POST http://localhost:8080/service-orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "clientId": 1,
    "vehicleId": 1,
    "serviceIds": [1],
    "items": [
      {
        "partId": 1,
        "quantity": 1
      }
    ]
  }'

# Resposta: { "id": 1, "status": "RECEIVED", "totalAmount": 195.00, ... }
```

### 7. Atualizar Status para "Em Diagnóstico"

```bash
# PATCH /service-orders/1/status
curl -X PATCH http://localhost:8080/service-orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "newStatus": "IN_DIAGNOSIS"
  }'
```

### 8. Atualizar Status para "Aguardando Aprovação"

```bash
# PATCH /service-orders/1/status
curl -X PATCH http://localhost:8080/service-orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "newStatus": "AWAITING_APPROVAL"
  }'

# Um email será enviado ao cliente com o orçamento
```

### 9a. Aprovar Orçamento

```bash
# POST /service-orders/1/approve
curl -X POST http://localhost:8080/service-orders/1/approve \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"

# Resposta: { "id": 1, "status": "IN_PROGRESS", ... }
# O estoque das peças é debitado automaticamente
# Um email de confirmação é enviado ao cliente
```

### 9b. Recusar Orçamento (Alternativa)

```bash
# POST /service-orders/1/refuse
curl -X POST http://localhost:8080/service-orders/1/refuse \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"

# Resposta: { "id": 1, "status": "REFUSED", ... }
# A ordem de serviço é finalizada
# Um email de recusa é enviado ao cliente
```

### 10. Consultar Status (Endpoint Público)

```bash
# GET /service-orders/status?document=12345678900&plate=ABC1234
curl -X GET "http://localhost:8080/service-orders/status?document=12345678900&plate=ABC1234"

# Resposta: { "orderId": 1, "status": "IN_PROGRESS" }
# Não requer autenticação
```

### 11. Finalizar Ordem de Serviço

```bash
# PATCH /service-orders/1/status
curl -X PATCH http://localhost:8080/service-orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "newStatus": "COMPLETED"
  }'

# PATCH /service-orders/1/status (Entrega)
curl -X PATCH http://localhost:8080/service-orders/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "newStatus": "DELIVERED"
  }'
    ```

---

## 🔗 Acessando os Serviços (Ambiente Kubernetes)

| Serviço                 | URL de Acesso                                | Como Aceder                                                                                                                                   |
| :---------------------- | :------------------------------------------- |:----------------------------------------------------------------------------------------------------------------------------------------------|
| 🚀 **API (Swagger UI)** | `http://localhost:8081/swagger-ui/index.html` | Aceda diretamente no navegador. A porta `8081` foi mapeada pelo Terraform.                                                                    |
| ✉️ **MailHog (Emails)** | `http://localhost:8025`                      | **Abra um novo terminal** e execute `kubectl port-forward svc/mailhog-service 8025:8025`. Mantenha o terminal aberto e acesse pelo navegador. |

### 🔑 Utilizando a API (Autenticação)

1.  No Swagger, execute o `POST /login` com o corpo:
    ```json
    {
      "username": "admin",
      "password": "password"
    }
    ```
2.  Copie o `token` JWT retornado.
3.  Clique no botão **"Authorize"**, cole o token no campo (precedido por `Bearer `) e confirme.
4.  Agora você pode executar todos os outros endpoints protegidos.

---

## 📊 Horizontal Pod Autoscaler (HPA)

O sistema utiliza HPA para escalar automaticamente os pods da aplicação baseado na utilização de CPU.

### Configuração do HPA

O HPA está configurado para:
- **Mínimo de réplicas:** 1
- **Máximo de réplicas:** 5
- **Métrica:** CPU com utilização média de 50%

### Comando de Carga Utilizado

Para testar o HPA, utilizamos o `kubectl run` com múltiplas requisições simultâneas:

```bash
# Gerar carga na aplicação
kubectl run load-generator --image=busybox --restart=Never -- /bin/sh -c \
  "while true; do wget -q -O- http://oficina-service:8080/service-orders; sleep 0.1; done"

# Ou usando Apache Bench (se disponível)
kubectl run ab-test --image=httpd --restart=Never -- ab -n 10000 -c 10 \
  http://oficina-service:8080/service-orders
```

### Monitoramento do HPA

```bash
# Verificar status do HPA
kubectl get hpa oficina-hpa

# Saída esperada:
# NAME           REFERENCE                 TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
# oficina-hpa    Deployment/oficina-app    50%/50%    1         5         3          5m
```

### Monitoramento de Recursos

```bash
# Ver utilização de recursos dos pods
kubectl top pods

# Saída esperada durante carga:
# NAME                              CPU(cores)   MEMORY(bytes)
# oficina-deployment-7d8f9c4b5-abc   250m         512Mi
# oficina-deployment-7d8f9c4b5-def   180m         480Mi
# oficina-deployment-7d8f9c4b5-ghi   220m         495Mi
```

### Resultados Esperados

Durante a execução da carga:
1. O HPA detecta aumento na utilização de CPU acima de 50%
2. Escala automaticamente de 1 para até 5 réplicas
3. Distribui o tráfego entre os pods
4. Quando a carga diminui, reduz gradualmente o número de réplicas

**Observação:** Para visualizar o HPA em ação, é necessário que o deployment tenha recursos de CPU definidos e o Metrics Server esteja instalado no cluster.

---

## 🔄 Fluxo de CI/CD

A pipeline no GitHub Actions é acionada a cada `push` na branch `main`. Ela executa os seguintes passos:
1.  **Testa:** Roda todos os testes unitários e de integração com Maven.
2.  **Constrói:** Gera a imagem Docker da aplicação.
3.  **Publica:** Envia a imagem para o GitHub Container Registry, pronta para ser usada em um deploy.

Você pode acompanhar as execuções na aba **[Actions](https://github.com/luscuissiatto/tech-challenge-oficina/actions)** do repositório.

---

## 🎯 Funcionalidades Implementadas

### Gestão de Orçamentos

O sistema agora possui rotas específicas para aprovação e recusa de orçamentos:

- **`POST /service-orders/{id}/approve`**: Aprova o orçamento de uma ordem de serviço
  - Transição: `AWAITING_APPROVAL` → `IN_PROGRESS`
  - Debita automaticamente o estoque das peças
  - Envia email de confirmação ao cliente

- **`POST /service-orders/{id}/refuse`**: Recusa o orçamento de uma ordem de serviço
  - Transição: `AWAITING_APPROVAL` → `REFUSED`
  - Finaliza a ordem de serviço
  - Envia email de recusa ao cliente

As transições de estado são validadas no agregado `ServiceOrder` com regras de negócio e eventos de domínio (`BudgetApprovedEvent` e `BudgetRefusedEvent`).