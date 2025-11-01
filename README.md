# BridalCover CRM

**BridalCover CRM** is a focused and lightweight Customer Relationship Management system for businesses selling garment covers to bridal and tuxedo rental shops.

This project was designed to not only support the daily workflow of such businesses, but also serve as a robust platform for learning modern software architecture, including microservices, event-driven communication, caching, and third-party API integration.

---

## 🎯 Goal

To intelligently prospect, manage, and visualize potential and current clients (especially bridal/tuxedo rental stores), using geolocation data and structured sales contact history.

---

## 📖 Documentação
Complete documentation is in the folder [`docs/`](docs/).

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
| Database | PostgreSQL or MongoDB |
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

### [ ] Step 9: Deploy and Monitor
- Docker + CI/CD
- Logs, Alerts, Usage metrics

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

### **Run Locally** *TODO*
```bash
# Clone the repository
git clone https://github.com/your-username/project-name.git
cd project-name

# Configure the database in application.properties

# Build and run the project

```
---
## 📌 Final Notes

BridalCover CRM focuses on **real-world business value** while helping the developer explore key concepts in modern software engineering. This system is ideal for entrepreneurs and developers looking to build smart, scalable tools tailored to niche markets.