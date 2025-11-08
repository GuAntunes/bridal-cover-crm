# Estrutura Hexagonal - Bridal Cover CRM

Este documento descreve a estrutura de pacotes do projeto, que segue a **Arquitetura Hexagonal (Ports and Adapters)**.

## 📐 Visão Geral

A Arquitetura Hexagonal organiza o código em camadas bem definidas, onde:
- O **domínio** (core business) é independente de frameworks e tecnologias
- As **portas** definem interfaces de entrada e saída
- Os **adaptadores** implementam as portas usando tecnologias específicas

## 📦 Estrutura de Pacotes

```
src/main/kotlin/br/com/gustavoantunes/bridalcovercrm/
├── domain/                       # 🎯 CAMADA DE DOMÍNIO
│   ├── port/                     # Interfaces (Portas)
│   │   ├── in/                   # Portas de entrada (casos de uso)
│   │   │   └── lead/
│   │   │       ├── GetLeadUseCase.kt
│   │   │       └── RegisterLeadUseCase.kt
│   │   └── out/                  # Portas de saída (repositórios, gateways)
│   │       └── repository/
│   │           └── LeadRepository.kt
│   ├── model/                    # Entidades, Value Objects e regras de negócio
│   │   ├── common/               # Componentes comuns do domínio
│   │   │   ├── AggregateId.kt
│   │   │   ├── AggregateRoot.kt
│   │   │   ├── AggregateRootWithId.kt
│   │   │   ├── DomainEvent.kt
│   │   │   └── DomainObject.kt
│   │   ├── lead/                 # Agregados de Lead
│   │   │   ├── Lead.kt
│   │   │   ├── LeadId.kt
│   │   │   ├── LeadSource.kt
│   │   │   └── LeadStatus.kt
│   │   └── shared/               # Value Objects compartilhados
│   │       ├── CNPJ.kt
│   │       ├── CompanyName.kt
│   │       ├── ContactInfo.kt
│   │       ├── Email.kt
│   │       ├── Phone.kt
│   │       └── SocialMediaType.kt
│   └── event/                    # Eventos de domínio (Domain Events)
│
├── application/                  # 🔄 CAMADA DE APLICAÇÃO
│   ├── usecase/                  # Implementação dos casos de uso
│   │   └── lead/
│   │       ├── GetLeadService.kt
│   │       └── RegisterLeadService.kt
│   └── dto/                      # DTOs para entrada/saída
│       └── lead/
│           ├── GetLeadQuery.kt
│           └── RegisterLeadCommand.kt
│
├── infrastructure/               # 🔧 CAMADA DE INFRAESTRUTURA
│   ├── adapter/                  # Implementações concretas dos adaptadores
│   │   ├── in/                   # Adaptadores de entrada
│   │   │   └── (controllers REST, GraphQL, etc)
│   │   └── out/                  # Adaptadores de saída
│   │       └── persistence/
│   │           ├── repository/
│   │           │   ├── LeadRepositoryAdapter.kt
│   │           │   └── LeadDataJdbcRepository.kt
│   │           ├── entity/
│   │           │   └── LeadEntity.kt
│   │           └── mapper/
│   │               ├── LeadMapper.kt
│   │               └── ContactInfoJsonMapper.kt
│   ├── config/                   # Configuração de frameworks (Spring, etc)
│   └── client/                   # Clientes de APIs externas
│
└── BridalCoverCrmApplication.kt  # Ponto de entrada da aplicação
```

## 🎯 Camada de Domínio

### **domain/port/**
Define as **interfaces** que representam as portas da aplicação:

#### **port/in/** - Portas de Entrada (Use Cases)
- Interfaces que definem os **casos de uso** da aplicação
- Representam as **ações** que podem ser executadas
- Exemplo: `RegisterLeadUseCase`, `GetLeadUseCase`
- **Convenção**: Interfaces terminam com `UseCase`

#### **port/out/** - Portas de Saída (Repositórios, Gateways)
- Interfaces que definem **dependências externas**
- Representam serviços que o domínio **precisa** mas não implementa
- Exemplo: `LeadRepository`
- **Convenção**: Interfaces terminam com `Repository`, `Gateway` ou `Port`

### **domain/model/**
Contém as **entidades**, **value objects** e **regras de negócio puras**:
- **Agregados**: Entidades principais (ex: `Lead`)
- **Value Objects**: Objetos imutáveis que representam conceitos do domínio (ex: `Email`, `CNPJ`)
- **Domain Objects**: Classes base para o modelo de domínio
- **Sem dependências** de frameworks ou infraestrutura

### **domain/event/**
Contém os **eventos de domínio**:
- Representam fatos que ocorreram no domínio
- Usados para comunicação entre bounded contexts
- Seguem o padrão Event Sourcing/CQRS

## 🔄 Camada de Aplicação

### **application/usecase/**
Contém a **implementação dos casos de uso**:
- Implementa as interfaces de `domain/port/in/`
- Orquestra o fluxo de negócios
- Usa as portas de saída (`domain/port/out/`) para acessar recursos externos
- **Convenção**: Classes terminam com `Service`
- **Anotação**: `@Service` do Spring

### **application/dto/**
Contém os **Data Transfer Objects**:
- Commands: Objetos de entrada para casos de uso
- Queries: Objetos de consulta
- Responses: Objetos de resposta (quando necessário)

## 🔧 Camada de Infraestrutura

### **infrastructure/adapter/in/**
Contém os **adaptadores de entrada**:
- **Controllers REST**: Expõem APIs HTTP
- **Controllers GraphQL**: Expõem APIs GraphQL
- **Message Listeners**: Consomem mensagens de filas
- **Convenção**: Classes terminam com `Controller`
- **Anotações**: `@RestController` ou `@Controller` do Spring

### **infrastructure/adapter/out/**
Contém os **adaptadores de saída**:

#### **persistence/**
- **repository/**: Implementações dos repositórios
  - `*Adapter.kt`: Implementa interfaces de `domain/port/out/`
  - `*DataJdbcRepository.kt`: Interfaces Spring Data
  - **Convenção**: Adapters terminam com `Adapter`
  - **Anotações**: `@Component` para adapters, `@Repository` para Spring Data

- **entity/**: Entidades de persistência
  - Modelos específicos do banco de dados
  - Anotadas com `@Table`, `@Entity`, etc
  - **Convenção**: Classes terminam com `Entity`

- **mapper/**: Conversores entre domínio e persistência
  - Converte entre `domain/model/` e `infrastructure/adapter/out/persistence/entity/`
  - **Convenção**: Objects terminam com `Mapper`

### **infrastructure/config/**
Contém as **configurações de frameworks**:
- Configuração do Spring
- Configuração de beans
- Configuração de segurança
- Configuração de banco de dados

### **infrastructure/client/**
Contém **clientes de APIs externas**:
- Implementações de integrações com serviços externos
- HTTP clients, gRPC clients, etc

## 🔀 Fluxo de Dados

### Request Flow (Entrada)
```
Controller (in) 
  → UseCase Interface (domain/port/in)
    → UseCase Implementation (application/usecase)
      → Repository Interface (domain/port/out)
        → Repository Adapter (infrastructure/adapter/out)
          → Domain Model
```

### Response Flow (Saída)
```
Domain Model
  → Repository Adapter
    → UseCase Implementation
      → Controller
        → HTTP Response
```

## 📋 Regras de Dependência

### ✅ Permitido
- `infrastructure` → `application` → `domain`
- Camadas externas **podem** depender de camadas internas
- Adaptadores implementam portas

### ❌ Proibido
- `domain` → `application` ou `infrastructure`
- `application` → `infrastructure`
- Camadas internas **não podem** depender de camadas externas
- Domain model não pode ter anotações de framework

## 🧪 Testes de Arquitetura

O projeto usa **ArchUnit** para garantir o cumprimento das regras arquiteturais:

```kotlin
// Exemplo de regras
- Use cases devem estar em application.usecase
- Ports devem estar em domain.port.in ou domain.port.out
- Adapters devem estar em infrastructure.adapter
- Domain não pode depender de application ou infrastructure
- Application não pode depender de infrastructure
```

## 🎨 Convenções de Nomenclatura

| Tipo | Pacote | Sufixo | Exemplo |
|------|--------|--------|---------|
| Use Case (Interface) | domain/port/in | `UseCase` | `RegisterLeadUseCase` |
| Use Case (Implementação) | application/usecase | `Service` | `RegisterLeadService` |
| Repository (Interface) | domain/port/out | `Repository` | `LeadRepository` |
| Repository (Implementação) | infrastructure/adapter/out | `Adapter` | `LeadRepositoryAdapter` |
| Spring Data Repository | infrastructure/adapter/out | `*DataJdbcRepository` | `LeadDataJdbcRepository` |
| Controller | infrastructure/adapter/in | `Controller` | `LeadController` |
| Entity (Domínio) | domain/model | sem sufixo | `Lead` |
| Entity (Persistência) | infrastructure/.../entity | `Entity` | `LeadEntity` |
| Mapper | infrastructure/.../mapper | `Mapper` | `LeadMapper` |

## 📚 Benefícios da Arquitetura Hexagonal

1. **Testabilidade**: Fácil criar testes unitários sem dependências externas
2. **Flexibilidade**: Trocar implementações sem afetar o domínio
3. **Manutenibilidade**: Código organizado e com responsabilidades claras
4. **Independência de Framework**: Domínio não depende de Spring, JPA, etc
5. **Domain-Driven Design**: Foco no domínio e regras de negócio

## 🔗 Referências

- [Arquitetura Hexagonal (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://domainlanguage.com/ddd/)







