# Bounded Contexts - BridalCover CRM

Os **Bounded Contexts** definem as fronteiras claras onde determinados modelos de domínio são válidos e consistentes. Cada contexto tem sua própria linguagem ubíqua e responsabilidades específicas.

---

## 🎯 Visão Geral dos Contextos

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Lead Management│    │ Sales Execution │    │Geographic Analytics│
│                 │    │                 │    │                 │
│ • Lead          │◄──►│ • ContactLog    │    │ • LeadDensity   │
│ • Client        │    │ • Script        │    │ • Territory     │
│ • Qualification │    │ • SalesProcess  │    │ • Heatmap       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         ▲                       ▲                       ▲
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 ▼
                    ┌─────────────────┐
                    │ External        │
                    │ Integration     │
                    │                 │
                    │ • GooglePlaces  │
                    │ • PlaceImport   │
                    └─────────────────┘
```

---

## 🏢 1. Lead Management Context

### **Responsabilidade Principal**
Gerenciar o ciclo de vida completo de prospects e clientes, desde a identificação até a conversão.

### **Entidades Principais**
- **Lead**: Prospect em potencial
- **Client**: Cliente convertido
- **Address**: Informações de localização
- **LeadSource**: Origem do prospect

### **Conceitos Específicos**
- **Lead Lifecycle**: Gestão do ciclo de vida do prospect
- **Lead Qualification**: Processo de qualificação
- **Conversion Process**: Transformação de lead em cliente
- **Client Retention**: Manutenção do relacionamento

### **Regras de Negócio**
- Um Lead só pode ser convertido uma vez
- Cliente deve manter informações de contato atualizadas
- Lead deve ter pelo menos um canal de contato válido
- Conversão requer aprovação do processo de qualificação

### **Eventos Publicados**
- `LeadCreated`
- `LeadQualified`
- `LeadConverted`
- `ClientUpdated`

### **Eventos Consumidos**
- `ContactSuccessful` (do Sales Execution)
- `TerritoryPrioritized` (do Geographic Analytics)

---

## 📞 2. Sales Execution Context

### **Responsabilidade Principal**
Executar e rastrear todas as atividades de vendas, incluindo contatos, scripts e follow-ups.

### **Entidades Principais**
- **ContactLog**: Registro de tentativas de contato
- **Script**: Roteiros de vendas
- **SalesAttempt**: Tentativa específica de venda
- **FollowUp**: Agendamento de próximos contatos

### **Conceitos Específicos**
- **Contact Orchestration**: Orquestração de contatos
- **Script Effectiveness**: Medição de efetividade
- **Sales Funnel**: Funil de vendas
- **Contact Strategy**: Estratégia de abordagem

### **Regras de Negócio**
- Máximo 3 tentativas consecutivas sem resposta
- Follow-up obrigatório para contatos positivos
- Script deve ser selecionado baseado no contexto do lead
- Efetividade calculada automaticamente após cada uso

### **Eventos Publicados**
- `ContactAttempted`
- `ContactSuccessful`
- `FollowUpScheduled`
- `ScriptUsed`

### **Eventos Consumidos**
- `LeadCreated` (do Lead Management)
- `TerritoryAnalyzed` (do Geographic Analytics)

---

## 🗺️ 3. Geographic Analytics Context

### **Responsabilidade Principal**
Analisar distribuição geográfica de leads e otimizar estratégias territoriais.

### **Entidades Principais**
- **Territory**: Região de vendas
- **LeadDensity**: Densidade de prospects por área
- **GeoAnalytics**: Análises geográficas
- **TerritoryPriority**: Priorização de regiões

### **Conceitos Específicos**
- **Heat Mapping**: Mapeamento de densidade
- **Territory Optimization**: Otimização territorial
- **Geographic Distribution**: Distribuição geográfica
- **Regional Performance**: Performance por região

### **Regras de Negócio**
- Densidade calculada baseada em leads ativos por km²
- Prioridade determinada por densidade e taxa de conversão
- Territórios devem ter limites geográficos definidos
- Análise atualizada semanalmente

### **Eventos Publicados**
- `TerritoryAnalyzed`
- `TerritoryPrioritized`
- `DensityCalculated`

### **Eventos Consumidos**
- `LeadCreated` (do Lead Management)
- `ContactSuccessful` (do Sales Execution)

---

## 🔗 4. External Integration Context

### **Responsabilidade Principal**
Gerenciar integrações com serviços externos, especialmente Google Places API.

### **Entidades Principais**
- **GooglePlacesData**: Dados importados do Google
- **ExternalLead**: Lead originado de fonte externa
- **ImportBatch**: Lote de importação
- **PlaceValidation**: Validação de estabelecimentos

### **Conceitos Específicos**
- **Data Import**: Importação de dados externos
- **Place Validation**: Validação de estabelecimentos
- **External Mapping**: Mapeamento de dados externos
- **Import Orchestration**: Orquestração de importações

### **Regras de Negócio**
- Apenas estabelecimentos com foco em aluguel de trajes
- Validação obrigatória antes da importação
- Deduplicação automática baseada em CNPJ/nome
- Limite de 100 importações por dia via API

### **Eventos Publicados**
- `PlacesImported`
- `ExternalLeadValidated`
- `ImportCompleted`

### **Eventos Consumidos**
- `TerritoryPrioritized` (do Geographic Analytics)

---

## 🔄 Context Map - Relacionamentos

### **Partnership (Parceria)**
- **Lead Management ↔ Sales Execution**: Colaboração estreita na gestão do funil de vendas

### **Customer-Supplier (Cliente-Fornecedor)**
- **Geographic Analytics → Lead Management**: Fornece análises para priorização
- **Sales Execution → Lead Management**: Atualiza status baseado em contatos
- **External Integration → Lead Management**: Fornece novos leads

### **Conformist (Conformista)**
- **External Integration → Google Places API**: Adapta-se ao modelo do Google

### **Anti-corruption Layer (Camada Anticorrupção)**
- **Lead Management ← External Integration**: Protege o modelo de domínio de mudanças externas

---

## 📋 Shared Kernel - Elementos Compartilhados

### **Value Objects Compartilhados**
- `Address`: Usado por todos os contextos
- `Coordinates`: Compartilhado entre Geographic e Lead Management
- `ContactInfo`: Utilizado por Lead Management e Sales Execution

### **Enums Compartilhados**
- `LeadStatus`: Compartilhado entre Lead Management e Sales Execution
- `ContactChannel`: Usado por Sales Execution e External Integration

### **Domain Events Compartilhados**
- Estrutura base de eventos
- Mecanismo de publicação/subscrição
- Timestamping e versionamento

---

## 🚧 Bounded Context Integration Patterns

### **Event-Driven Integration**
- Comunicação assíncrona via domain events
- Garante baixo acoplamento entre contextos
- Permite evolução independente

### **API Gateway Pattern**
- Ponto único de entrada para integrações externas
- Centraliza autenticação e rate limiting
- Facilita monitoramento e observabilidade

### **CQRS per Context**
- Command e Query separados por contexto
- Permite otimizações específicas
- Facilita evolução independente dos modelos

---

## 📝 Considerações de Design

### **Autonomia dos Contextos**
- Cada contexto pode evoluir independentemente
- Decisões tecnológicas podem ser diferentes
- Equipes podem trabalhar de forma autônoma

### **Consistência Eventual**
- Aceitação de inconsistências temporárias
- Propagação de mudanças via eventos
- Mecanismos de compensação quando necessário

### **Resiliência**
- Falhas em um contexto não afetam outros
- Timeout e circuit breaker patterns
- Fallback strategies para integrações externas

---

**Nota**: Esta estrutura de bounded contexts deve ser revisada conforme o sistema evolui. Novos contextos podem emergir e fronteiras podem ser ajustadas baseado no aprendizado do domínio. 