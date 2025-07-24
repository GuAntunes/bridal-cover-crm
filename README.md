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

# Hexagonal Architecture

**Hexagonal Architecture** aims to isolate the **domain** of the application from external dependencies (such as infrastructure, frameworks, and databases), allowing the system to be more flexible, testable, and maintainable with business logic independent of technologies.

## Layer Structure

### 1. **Domain (`domain`)**
- **Defines the ports (interfaces)** that external layers (such as application and infrastructure) should implement.
- Contains **business rules** and domain models. It does not depend on any external technology.
- **Types of Ports:**
    - **Inbound Ports (Driving Ports):** Interfaces representing use cases.
    - **Outbound Ports (Driven Ports):** Interfaces for interaction with external systems, such as repositories or external services.

### 2. **Application Layer (`application`)**
- **Interacts with the domain** via the **inbound ports** defined in the domain.
- **Orchestrates the use cases** but does not contain business logic.
- **Calls outbound ports** to interact with infrastructure, such as saving data or making calls to external services.

### 3. **Infrastructure Layer (`infrastructure`)**
- **Implements outbound ports** defined by the domain.
- Contains the implementation of **adapters**, such as database repositories, external services, and other dependencies.
- **Does not directly interact with the application layer**, ensuring business logic is independent of external technologies.

## Interaction Flow:
1. The **Application Layer** calls an **inbound port** (use case).
2. The **Domain** executes business logic and interacts with **outbound ports**.
3. The **Infrastructure** layer implements the outbound ports and interacts with external systems (e.g., databases, APIs).

## Benefits:
- **Decoupling**: The domain does not depend on external databases, frameworks, or libraries.
- **Testability**: Business logic can be easily tested in isolation without dependencies on infrastructure.
- **Flexibility**: External technologies (such as databases or frameworks) can be swapped without impacting the core business logic.

# Packages Structure
```
├── application   // Lógica de negócio (casos de uso)
│   ├── port
│   │   ├── in    // Interfaces para entrada (casos de uso)
│   │   ├── out   // Interfaces para saída (repositórios, gateways)
│   ├── service   // Implementação dos casos de uso
│
├── domain        // Entidades e regras de negócio puras
│   ├── model     // Modelos de domínio
│   ├── event     // Eventos de domínio (opcional)
│
├── infrastructure // Adapters (banco de dados, APIs externas, frameworks)
│   ├── repository // Implementação das portas de saída
│   ├── controller // Controladores (caso use REST)
│   ├── config     // Configuração de Beans (caso use Spring)
│   ├── client     // Comunicação com serviços externos
│
└── main          // Inicialização da aplicação
```
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