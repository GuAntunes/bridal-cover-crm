# C4 Model Documentation - BridalCover CRM

Este documento descreve os diagramas C4 Model criados para documentar a arquitetura do sistema BridalCover CRM. O C4 Model oferece uma abordagem hierárquica para visualizar arquitetura de software através de 4 níveis de abstração.

---

## 📊 Visão Geral dos Níveis C4

### **Nível 1 - System Context**
- **Propósito**: Mostra como o sistema se encaixa no mundo
- **Audiência**: Todos os stakeholders 
- **Zoom**: 10.000 metros - visão panorâmica

### **Nível 2 - Container**  
- **Propósito**: Mostra a arquitetura de alto nível do sistema
- **Audiência**: Arquitetos, desenvolvedores sênior
- **Zoom**: 1.000 metros - componentes principais

### **Nível 3 - Component**
- **Propósito**: Mostra como um container é estruturado internamente  
- **Audiência**: Desenvolvedores, arquitetos de software
- **Zoom**: 100 metros - estrutura interna

### **Nível 0 - System Landscape**
- **Propósito**: Mostra o ecossistema de sistemas da empresa
- **Audiência**: Executivos, arquitetos empresariais
- **Zoom**: 50.000 metros - visão corporativa

---

## 🌍 System Landscape (`c4-landscape.puml`)

### **Descrição**
Mostra o **ecossistema empresarial completo** onde o BridalCover CRM opera, incluindo sistemas internos, externos e pessoas envolvidas.

### **Elementos Principais**

#### **Enterprise: BridalCover Enterprise**
- **Pessoas**: Sales Rep, Sales Manager, Marketing Manager, Data Analyst, SysAdmin
- **Sistema Core**: BridalCover CRM
- **Sistemas Suporte**: ERP, BI, Email Marketing, Call Center, Mobile Workforce

#### **Provedores Externos**
- **Google Cloud Platform**: Places API, Maps, Analytics
- **Communication Providers**: SendGrid, Twilio, WhatsApp, Mailchimp  
- **Cloud Infrastructure**: AWS, Auth0, Datadog, Sentry
- **Financial Services**: Payment Gateway, Banking, Invoice Management

### **Valor de Negócio**
- **Visão holística** do ecossistema tecnológico
- **Identificação de dependências** externas críticas
- **Mapeamento de integrações** necessárias
- **Análise de riscos** de fornecedores

---

## 🎯 System Context (`c4-context.puml`)

### **Descrição**
Foca especificamente no **BridalCover CRM** e suas **interações diretas** com usuários e sistemas externos.

### **Elementos Principais**

#### **Pessoas**
- **Vendedor**: Usuário primário - prospecção e contatos
- **Gerente de Vendas**: Supervisão e análise estratégica  
- **Proprietário de Loja**: Cliente-alvo - recebe contatos

#### **Sistema Principal**
- **BridalCover CRM**: Sistema central especializado

#### **Sistemas Externos**
- **Google Places/Maps**: Fonte de dados de estabelecimentos
- **Serviços de Comunicação**: Email, SMS, WhatsApp
- **Autenticação**: Auth0/OAuth2

### **Funcionalidades Core**
- Gestão de leads e clientes
- Prospecção inteligente via Google Places
- Controle de contatos multi-canal
- Scripts de vendas com métricas
- Análise territorial e heatmaps

---

## 🏗️ Container Diagram (`c4-container.puml`)

### **Descrição**
Mostra a **arquitetura interna** do BridalCover CRM, detalhando containers (aplicações/serviços) e suas responsabilidades.

### **Containers Frontend**
- **Web Application** (React/TypeScript): Interface web responsiva
- **Mobile App** (React Native): App para vendedores em campo

### **Containers Backend** 
- **API Gateway** (Spring Cloud Gateway): Entrada única, autenticação
- **Lead Management Service** (Spring Boot/Kotlin): Bounded Context para leads/clientes
- **Sales Execution Service** (Spring Boot/Kotlin): Bounded Context para contatos/scripts
- **Geographic Analytics Service** (Spring Boot/Kotlin): Bounded Context para análise territorial  
- **External Integration Service** (Spring Boot/Kotlin): Bounded Context para integrações

### **Containers de Dados**
- **Main Database** (PostgreSQL): Dados principais ACID
- **Cache Database** (Redis): Cache de alta performance
- **Event Store** (PostgreSQL): Eventos de domínio

### **Containers de Messaging**
- **Message Broker** (Apache Kafka): Eventos entre bounded contexts
- **Task Queue** (RabbitMQ): Processamento assíncrono

### **Containers de Observabilidade**
- **Monitoring Service** (Prometheus/Grafana): Métricas e alertas
- **Logging Service** (ELK Stack): Centralização de logs

### **Padrões Arquiteturais**
- **Microservices**: Bounded contexts independentes
- **Event-Driven Architecture**: Comunicação assíncrona
- **CQRS**: Separação comando/consulta
- **Hexagonal Architecture**: Isolamento de domínio

---

## 🔧 Component Diagram (`c4-component.puml`)

### **Descrição**
Detalha a **estrutura interna** do **Lead Management Service**, mostrando implementação da Hexagonal Architecture.

### **Controllers (Adapters de Entrada)**
- **Lead Controller**: Endpoints REST para leads
- **Client Controller**: Endpoints REST para clientes  
- **Conversion Controller**: Endpoints para conversões

### **Application Services (Casos de Uso)**
- **Lead Service**: Orquestra operações de leads
- **Client Service**: Gerencia clientes
- **Conversion Service**: Processa conversões lead→cliente
- **Qualification Service**: Qualificação de leads

### **Domain Layer**
- **Lead Aggregate**: Aggregate root com regras de negócio
- **Client Aggregate**: Aggregate root para clientes
- **Address Value Object**: Objeto de valor para endereços
- **Lead Qualification**: Domain service para qualificação

### **Ports (Interfaces)**
- **Inbound Ports**: Lead Management Port, Client Management Port
- **Outbound Ports**: Repository Ports, Event Publisher Port

### **Adapters (Infrastructure)**
- **Repositories**: JPA implementations para persistência
- **Cache**: Redis adapter para performance
- **Event Publisher**: Kafka adapter para eventos
- **Event Handlers**: Processamento de eventos externos

### **Padrões de Design**
- **Ports & Adapters**: Hexagonal Architecture
- **Repository Pattern**: Abstração de persistência
- **Domain Events**: Comunicação entre contextos
- **CQRS**: Separação comando/consulta

---

## 🎨 Convenções Visuais

### **Cores por Tipo**
- **🔵 Azul**: Pessoas (usuários do sistema)
- **🟢 Verde**: Sistemas internos da empresa
- **🔴 Vermelho**: Sistemas externos/terceiros
- **🟡 Amarelo**: Databases e armazenamento
- **🟣 Roxo**: Sistemas de messaging

### **Notação de Relacionamentos**
- **Seta sólida**: Interação síncrona (HTTP/REST)
- **Seta tracejada**: Interação assíncrona (eventos)
- **Linha dupla**: Fluxo de dados bidirecional
- **Texto na seta**: Protocolo/tecnologia utilizada

### **Boundaries (Fronteiras)**
- **System Boundary**: Delimita um sistema/aplicação
- **Enterprise Boundary**: Delimita organização/empresa
- **Container Boundary**: Delimita processo/serviço

---

## 📋 Uso dos Diagramas

### **Para Apresentações Executivas**
1. **System Landscape**: Visão estratégica do ecossistema
2. **System Context**: Escopo e valor do projeto

### **Para Arquitetos e Tech Leads**
1. **Container Diagram**: Decisões arquiteturais
2. **Component Diagram**: Estrutura técnica detalhada

### **Para Desenvolvedores**
1. **Component Diagram**: Implementação de bounded contexts
2. **Container Diagram**: Integrações e APIs

### **Para DevOps/SRE**
1. **Container Diagram**: Deployment e infraestrutura
2. **System Context**: Dependências externas

---

## 🔄 Evolução dos Diagramas

### **Versionamento**
- Diagramas devem ser versionados junto com o código
- Mudanças arquiteturais requerem atualização
- Review obrigatório para alterações

### **Sincronização**
- **Code-First**: Código como fonte da verdade
- **Documentation-Driven**: Diagramas guiam implementação
- **Living Documentation**: Atualização contínua

### **Ferramentas**
- **PlantUML**: Diagramas como código
- **C4-PlantUML**: Library específica para C4
- **Git**: Versionamento e colaboração
- **CI/CD**: Validação automática de diagramas

---

## 📚 Referências

- **C4 Model**: [c4model.com](https://c4model.com)
- **C4-PlantUML**: [github.com/plantuml-stdlib/C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML)
- **Domain-Driven Design**: Evans, Eric
- **Hexagonal Architecture**: Alistair Cockburn
- **Microservices Patterns**: Chris Richardson

---

**Nota**: Estes diagramas C4 complementam a documentação DDD existente, oferecendo múltiplas perspectivas arquiteturais para diferentes audiências e propósitos. 