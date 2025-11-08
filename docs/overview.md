# 📘 Visão Geral do Projeto - BridalCover CRM

## 🎯 O que é este projeto?

Um **CRM (Customer Relationship Management)** para fabricantes de **capas de vestidos de noiva** gerenciarem seus clientes: lojas de aluguel de trajes.

### Problema que resolve

Fabricantes de capas para vestidos de noiva precisam:
- Gerenciar leads (lojas potenciais)
- Acompanhar tentativas de contato
- Converter leads em clientes
- Organizar informações de forma centralizada

### Objetivo Principal

Aprender arquitetura de software moderna aplicando DDD, Arquitetura Hexagonal e boas práticas, enquanto resolve um problema real de negócio.

---

## 📊 Status Atual (O que está pronto)

### ✅ Implementado

**Domínio**
- Agregado `Lead` com Value Objects (CNPJ, Email, Phone, ContactInfo)
- Status de Lead: NEW, CONTACTED, QUALIFIED, PROPOSAL_SENT, NEGOTIATING, CONVERTED, LOST
- Fontes de Lead: MANUAL_ENTRY, GOOGLE_PLACES, REFERRAL, WEBSITE, COLD_CALL

**API REST**
- `POST /api/v1/leads` - Cadastrar lead
- `GET /api/v1/leads/{id}` - Buscar lead por ID
- `GET /health` - Health check

**Infraestrutura**
- PostgreSQL (banco de dados)
- Flyway (migrations)
- Docker Compose (ambiente local)
- Jenkins (CI/CD básico)
- Swagger/OpenAPI (documentação da API)
- ArchUnit (testes de arquitetura)

---

## 🎯 Próximos Passos

### Fase 1: Completar CRUD de Lead (próxima)
- [ ] Atualizar lead
- [ ] Deletar lead  
- [ ] Listar leads com paginação
- [ ] Buscar leads por status

### Fase 2: Gestão de Contatos
- [ ] Registrar tentativas de contato
- [ ] Ver histórico de contatos
- [ ] Agendar follow-ups

### Fase 3: Frontend
- [ ] Interface React simples
- [ ] Formulário de cadastro
- [ ] Lista de leads
- [ ] Dashboard básico

### Fase 4: Integrações
- [ ] Google Places API (buscar lojas)
- [ ] Importar leads automaticamente

---

## 💼 Contexto de Negócio

### Quem usa?

**Vendedores** de capas para vestidos de noiva que precisam:
- Prospectar lojas de aluguel de trajes
- Fazer contatos telefônicos/email
- Acompanhar negociações
- Converter prospects em clientes

### Fluxo Típico

```
1. Cadastrar Lead (loja de aluguel de vestidos)
   ↓
2. Fazer contatos (telefone, email, WhatsApp)
   ↓
3. Qualificar (avaliar potencial de compra)
   ↓
4. Enviar proposta comercial
   ↓
5. Negociar condições
   ↓
6. Converter em Cliente
```

---

## 🏗️ Arquitetura

Seguimos **Arquitetura Hexagonal (Ports & Adapters)** com **DDD**:

```
📦 Domain (regras de negócio)
   ├── Lead (agregado)
   ├── Value Objects (CNPJ, Email, Phone)
   └── Ports (interfaces)

📦 Application (casos de uso)
   ├── RegisterLeadService
   ├── GetLeadService
   └── Commands/Queries

📦 Infrastructure (tecnologia)
   ├── LeadController (REST API)
   ├── LeadRepositoryAdapter (PostgreSQL)
   └── Configurações (Spring, CORS, etc)
```

**Benefício:** Lógica de negócio isolada, fácil de testar e trocar tecnologias.

---

## 📚 Conceitos Principais (Linguagem Ubíqua)

| Termo | Significado |
|-------|-------------|
| **Lead** | Loja de aluguel potencial que ainda não comprou |
| **Cliente** | Loja que já comprou e mantém relacionamento |
| **Conversão** | Transformar lead em cliente (venda fechada) |
| **Qualificação** | Avaliar se lead tem potencial real |
| **Contato** | Tentativa de comunicação (telefone, email, WhatsApp) |
| **Follow-up** | Retorno agendado após contato |

---

## 🚀 Como usar este projeto

### Para estudar arquitetura
1. Veja a estrutura de pacotes seguindo DDD
2. Entenda a separação Domain → Application → Infrastructure
3. Observe como Value Objects garantem validações
4. Teste como ArchUnit garante regras arquiteturais

### Para aprender Spring Boot + Kotlin
1. Veja uso de Spring Data JDBC (sem JPA)
2. Entenda migrations com Flyway
3. Veja API-First com OpenAPI
4. Docker Compose para ambiente completo

### Para desenvolver
1. Siga o [Getting Started](development/getting-started.md)
2. Execute `make start-all` e `make run`
3. Acesse Swagger UI: http://localhost:8080/swagger-ui.html
4. Implemente novos casos de uso

---

## 📖 Documentação Completa

- **[Getting Started](development/getting-started.md)** - Setup local
- **[Hexagonal Structure](architecture/hexagonal-structure.md)** - Estrutura do código
- **[Deployment Guide](deployment/deployment-guide.md)** - Como deployar

---

**Resumo:** Projeto educacional que resolve problema real, usando arquitetura moderna e boas práticas. Foco em aprender fazendo! 🚀
