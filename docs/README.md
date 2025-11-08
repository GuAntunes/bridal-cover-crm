# 📚 Bridal Cover CRM — Documentation

Documentação completa do projeto Bridal Cover CRM, organizada por fase de desenvolvimento e propósito.

---

## 🚀 Quick Start

**Novo no projeto?** Comece aqui:

1. 📖 [Getting Started](development/getting-started.md) - Setup local e primeiros passos
2. 🏗️ [Arquitetura Hexagonal](architecture/hexagonal-structure.md) - Entenda a estrutura do código
3. 📝 [Linguagem Ubíqua](ubiquitous-language.md) - Glossário do domínio
4. 🎯 [Casos de Uso](use-cases.md) - Funcionalidades do sistema

---

## 📂 Estrutura da Documentação

### 1️⃣ **Business Context** (Negócio)
Entenda o problema e o domínio:

- **[overview.md](overview.md)** — Contexto do projeto e objetivos
- **[ubiquitous-language.md](ubiquitous-language.md)** — Glossário DDD
- **[use-cases.md](use-cases.md)** — Casos de uso detalhados

### 2️⃣ **Architecture** (Arquitetura)
Como o sistema é estruturado:

- **[hexagonal-structure.md](architecture/hexagonal-structure.md)** ⭐ — Estrutura do código (ESSENCIAL)
- **[models/](models/)** — Diagramas PlantUML
  - `overview.puml` — Visão geral do projeto
  - `domain-ddd.puml` — Modelo de domínio DDD

### 3️⃣ **Development** (Desenvolvimento)
Guias para desenvolvedores:

- **[getting-started.md](development/getting-started.md)** ⭐ — Setup local (COMECE AQUI)
- **[API Documentation](http://localhost:8080/swagger-ui.html)** — Swagger UI (quando rodando)

### 4️⃣ **Technologies** (Tecnologias Atuais)
Tecnologias em uso:

- **[swagger.md](technologies/swagger.md)** — OpenAPI e API-First
- **[arch-unit.md](technologies/arch-unit.md)** — Testes de arquitetura
- **[persistence-architecture.md](technologies/persistence-architecture.md)** — Persistência
- **[persistence-comparison.md](technologies/persistence-comparison.md)** — Comparação de abordagens

### 5️⃣ **Deployment** (Deploy)
Como deployar:

- **[deployment-guide.md](deployment/deployment-guide.md)** — Deploy local e CI/CD
- **[jenkins-guide.md](jenkins-guide.md)** — Setup do Jenkins

### 6️⃣ **Future Plans** 🔮
Documentação para o futuro:

- **[future/](future/)** — Tecnologias avançadas (Kubernetes, ArgoCD, Domain Events, etc.)
  - Estas tecnologias serão implementadas quando o projeto estiver mais maduro

---

## 🎯 Status Atual do Projeto

### ✅ Implementado (o que funciona AGORA)
- **Arquitetura Hexagonal** com Domain, Application e Infrastructure
- **Agregado Lead** com Value Objects (CNPJ, Email, Phone, ContactInfo)
- **API REST:**
  - `POST /api/v1/leads` - Cadastrar lead
  - `GET /api/v1/leads/{id}` - Buscar lead
  - `GET /health` - Health check
- **OpenAPI/Swagger** - Documentação interativa da API
- **PostgreSQL** com Spring Data JDBC
- **Flyway** para migrations
- **Docker Compose** para ambiente local
- **Jenkins** CI/CD básico
- **ArchUnit** garantindo regras arquiteturais
- **CORS** configurado
- **Tratamento global de erros**

### 🔄 Próximos Passos (por ordem de prioridade)
1. **Completar CRUD de Lead**
   - Update (atualizar dados)
   - Delete (remover)
   - List (listar com paginação)
   - Search (buscar por filtros)

2. **Contact Management**
   - Registrar tentativas de contato
   - Histórico de interações
   - Agendar follow-ups

3. **Frontend React**
   - Dashboard simples
   - Formulários de cadastro
   - Lista de leads

4. **Integrações**
   - Google Places API
   - Importação automática de leads

---

## 📊 Diagramas Essenciais

### 🏢 `overview.puml` - Visão Geral
- Status do projeto
- O que está feito e próximos passos

### 🎯 `domain-ddd.puml` - Modelo DDD
- Agregados, Entidades e Value Objects
- Design do domínio

**Para ver os diagramas:** Use plugins PlantUML no VSCode ou IntelliJ

---

## 🔍 Navegação Rápida

### Para Desenvolvedores
```
📖 Getting Started → 🏗️ Hexagonal Structure → 💻 Swagger UI → 🧪 Testes
```

### Para Arquitetos
```
📝 Overview → 🎯 Use Cases → 🏗️ Architecture → 📊 Diagrams
```

### Para DevOps
```
🚀 Deployment Guide → 🔧 Jenkins Guide → 📦 Docker Compose
```

---

## 💡 Princípios do Projeto

1. **API-First**: Contrato OpenAPI definido antes do código
2. **DDD**: Domain-Driven Design com Arquitetura Hexagonal
3. **Clean Code**: Separação clara de responsabilidades
4. **Test-Driven**: Testes garantem qualidade arquitetural
5. **Pragmatic**: Implementar quando necessário, não porque é legal

---

## 📚 Referências Externas

- [Hexagonal Architecture (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://domainlanguage.com/ddd/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [OpenAPI Specification](https://swagger.io/specification/)

---

**Dúvidas?** Comece pelo [Getting Started](development/getting-started.md) e explore o [Swagger UI](http://localhost:8080/swagger-ui.html) 🚀
