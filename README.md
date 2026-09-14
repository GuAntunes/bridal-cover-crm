# BridalCover CRM

**BridalCover CRM** is a focused and lightweight Customer Relationship Management system for businesses selling garment covers to bridal and tuxedo rental shops.

This project was designed to not only support the daily workflow of such businesses, but also serve as a robust platform for learning modern software architecture, including microservices, event-driven communication, caching, and third-party API integration.

---

## 🎯 Goal

To intelligently prospect, manage, and visualize potential and current clients (especially bridal/tuxedo rental stores), using geolocation data and structured sales contact history.

---

## 📖 Documentação
Complete documentation is in the folder [`docs/`](docs/).

Start with [`docs/README.md`](docs/README.md) for a complete guide.

---

## 💡 Core Features

### 1. Client & Lead Management
- Register clients and leads with relevant details (name, CNPJ, city, type).
- Mark as “Client” or “Lead.”

### 2. Contact Tracking
- Record contact attempts (date, channel, result, notes).
- View history of interactions per lead.

### 3. Smart Lead Generation (Google Integration)
- Automatically search bridal/tuxedo stores using **Google Places API**.
- Import name, location, and phone from Google results.
- Highlight leads not yet in the system.

### 4. Map-based Heat Visualization
- Show city/region maps with **lead density heatmaps**.
- Prioritize high-density areas for in-person visits.

### 5. Sales Script & Interaction Notes
- Associate custom or default sales scripts per lead.
- Record lead responses and script usage effectiveness.

---

## 🧱 Technology Stack

| Component | Tech |
|----------|------|
| Backend | Kotlin + Spring Boot |
| Database | PostgreSQL + Spring Data JDBC |
| Migration | Flyway |
| CI/CD | Jenkins |
| Containerization | Docker + Docker Compose |
| Container Registry | Docker Hub |
| Orchestration | Kubernetes (repo [platform-gitops](https://github.com/GuAntunes/platform-gitops)) |
| Caching (optional) | Redis |
| Messaging (optional) | Kafka |
| External API | Google Places API |
| Map UI | Google Maps or Leaflet |
| Frontend | React or Jetpack Compose Desktop |

---

## 📝 Development Plan (Checkpoints)

### [ ] Step 1: Define Data Models
- Lead, Client, ContactLog, Script

### [ ] Step 2: Setup Kotlin Spring Boot Backend
- CRUD APIs for Lead, ContactLog, Script

### [ ] Step 3: Integrate Google Places API
- Endpoint to fetch stores from given city

### [ ] Step 4: Basic Frontend / Admin Panel
- Register & list clients/leads
- Track interactions

### [ ] Step 5: Map View for Lead Density
- Heatmap by city/street (Google Maps or Leaflet)

### [ ] Step 6: Record Sales Attempts
- Form + logs for sales attempts & script usage

### [ ] Step 7: Add Caching Layer (Redis)
- Store results from Google for faster lookup

### [ ] Step 8: Add Kafka for Async Events (Optional)
- Contact attempted, lead updated, etc.

### [x] Step 9: Deploy and Monitor
- ✅ Docker + Docker Compose configurado
- ✅ Jenkins CI/CD configurado
- ✅ Docker Hub para registry de imagens
- ✅ Imagens Docker por ambiente (deploy K8s no repo platform-gitops)
- [ ] Logs, Alerts, Usage metrics

---

# 🏗️ Arquitetura Hexagonal (Ports and Adapters)

O projeto segue a **Arquitetura Hexagonal**, que isola o **domínio** da aplicação de dependências externas (infraestrutura, frameworks, bancos de dados), permitindo que o sistema seja mais flexível, testável e mantível, com a lógica de negócio independente de tecnologias.

## 📐 Estrutura de Camadas

### 1. **Domain (`domain/`)**
- **Define as portas (interfaces)** que as camadas externas devem implementar
- Contém **regras de negócio** e modelos de domínio
- **Não depende** de nenhuma tecnologia externa
- **Tipos de Portas:**
  - **domain/port/in/**: Interfaces que representam casos de uso (driving ports)
  - **domain/port/out/**: Interfaces para interação com sistemas externos (driven ports)
- **domain/model/**: Entidades, Value Objects e agregados
- **domain/event/**: Eventos de domínio

### 2. **Application (`application/`)**
- **Implementa os casos de uso** definidos em `domain/port/in/`
- **Orquestra o fluxo de negócio** mas não contém lógica de domínio
- **Usa as portas de saída** para interagir com infraestrutura
- **application/usecase/**: Implementações dos casos de uso
- **application/dto/**: Data Transfer Objects (Commands, Queries)

### 3. **Infrastructure (`infrastructure/`)**
- **Implementa as portas de saída** definidas pelo domínio
- Contém adaptadores concretos (banco de dados, APIs externas, controllers)
- **infrastructure/adapter/in/**: Adaptadores de entrada (Controllers, Listeners)
- **infrastructure/adapter/out/**: Adaptadores de saída (Repositórios, Clients)
- **infrastructure/config/**: Configuração de frameworks

## 🔄 Fluxo de Interação

```
Controller (in) 
  → UseCase Interface (domain/port/in) 
    → UseCase Implementation (application/usecase)
      → Repository Interface (domain/port/out)
        → Repository Adapter (infrastructure/adapter/out)
          → Database
```

## ✅ Benefícios

- **Desacoplamento**: O domínio não depende de bancos de dados, frameworks ou bibliotecas externas
- **Testabilidade**: Lógica de negócio pode ser testada isoladamente
- **Flexibilidade**: Tecnologias externas podem ser trocadas sem impactar o core
- **Domain-Driven Design**: Foco total nas regras de negócio

## 📦 Estrutura de Pacotes

```
src/main/kotlin/br/com/gustavoantunes/bridalcovercrm/
├── domain/                       # 🎯 CAMADA DE DOMÍNIO
│   ├── port/                     # Interfaces (Portas)
│   │   ├── in/                   # Portas de entrada (casos de uso)
│   │   └── out/                  # Portas de saída (repositórios, gateways)
│   ├── model/                    # Entidades e Value Objects
│   └── event/                    # Eventos de domínio
│
├── application/                  # 🔄 CAMADA DE APLICAÇÃO
│   ├── usecase/                  # Implementação dos casos de uso
│   └── dto/                      # DTOs (Commands, Queries)
│
├── infrastructure/               # 🔧 CAMADA DE INFRAESTRUTURA
│   ├── adapter/
│   │   ├── in/                   # Adaptadores de entrada (Controllers)
│   │   └── out/                  # Adaptadores de saída (Repositórios, Clients)
│   ├── config/                   # Configuração de frameworks
│   └── client/                   # Clientes de APIs externas
│
└── BridalCoverCrmApplication.kt  # Ponto de entrada
```

Para mais detalhes sobre a estrutura, veja: [`docs/architecture/hexagonal-structure.md`](docs/architecture/hexagonal-structure.md)
---

## 🚀 Como Executar Localmente

### Pré-requisitos
- Docker e Docker Compose instalados
- Java 17 ou superior (para desenvolvimento local)
- Make (opcional, para usar comandos simplificados)

### Início Rápido

```bash
# Clone o repositório
git clone https://github.com/your-username/bridal-cover-crm.git
cd bridal-cover-crm

# Iniciar todos os serviços (Database + Jenkins)
make start-all

# Ou use Docker Compose diretamente
docker-compose up -d

# Build da aplicação
make build

# Executar a aplicação
make run
```

### Serviços Disponíveis

Após executar `make start-all`, você terá acesso a:

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| PostgreSQL | `localhost:5432` | user: `postgres`, pass: `postgres` |
| PgAdmin | http://localhost:8081 | email: `admin@bridalcrm.com`, pass: `admin123` |
| Jenkins | http://localhost:9090 | Ver comando abaixo para obter senha |
| API (quando rodando) | http://localhost:8080 | - |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |

### Comandos Úteis

```bash
# Ver todos os comandos disponíveis
make help

# Apenas banco de dados
make db-up
make db-down

# Apenas Jenkins
make jenkins-up
make jenkins-down
make jenkins-password  # Obter senha inicial

# Testes
make test           # Todos os testes
make arch-test      # Apenas testes de arquitetura

# Build
make build          # Build completo
make clean          # Limpar artefatos
```

### Configuração do Jenkins

Para configurar o Jenkins pela primeira vez:

1. Inicie o Jenkins: `make jenkins-up`
2. Obtenha a senha inicial: `make jenkins-password`
3. Acesse http://localhost:9090
4. Siga o guia completo em [`docs/jenkins-guide.md`](docs/jenkins-guide.md)

### Estrutura do Projeto

```
bridal-cover-crm/
├── src/                      # Código fonte
├── docs/                     # Documentação
├── scripts/                  # Scripts auxiliares
├── build.gradle.kts          # Configuração Gradle
├── docker-compose.yml        # Serviços Docker
├── Dockerfile                # Build da aplicação
├── Jenkinsfile              # Pipeline CI/CD
└── Makefile                  # Comandos simplificados
```

---
## 🚀 Deploy para Produção

Manifests Helm, Argo CD e overlays por ambiente ficam no repositório **[platform-gitops](https://github.com/GuAntunes/platform-gitops)**. Este repo contém apenas o código da aplicação e a publicação de imagens Docker.

### 🐳 Build de Imagens por Ambiente

```bash
# Ver todos os comandos
make help

# DEV
make docker-release-dev      # Build multi-plataforma + push (dev-latest)

# STAGING
make docker-release-staging  # Build multi-plataforma + push (staging-latest)

# PROD
make docker-release-prod     # Build multi-plataforma + push (latest)

# Todos os ambientes
make docker-release-all      # Build e push de todos
```

### 🔄 Workflow de release

```bash
# 1. Build e push da imagem
make docker-release-dev

# 2. Atualizar tag/imagem no platform-gitops e sync via Argo CD
```

**📚 Documentação:**
- 🐳 [Docker Hub Guide](docs/deployment/docker-hub-guide.md) - Build e push de imagens
- 🚀 [Deployment Guide](docs/deployment/deployment-guide.md) - CI/CD local e Jenkins
- ☸️ [platform-gitops](https://github.com/GuAntunes/platform-gitops) - Helm, overlays e Argo CD

---
## 📌 Final Notes

BridalCover CRM focuses on **real-world business value** while helping the developer explore key concepts in modern software engineering. This system is ideal for entrepreneurs and developers looking to build smart, scalable tools tailored to niche markets.