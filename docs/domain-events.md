# Domain Events - BridalCover CRM

Os **Domain Events** representam acontecimentos significativos no domínio que são relevantes para outras partes do sistema. Eles implementam comunicação assíncrona entre bounded contexts, garantindo baixo acoplamento.

---

## 🎯 Conceitos Fundamentais

### **Domain Event**
- Representa algo que aconteceu no passado
- É imutável após criação
- Carrega informações mínimas necessárias
- Nome sempre no tempo passado (ex: `LeadCreated`)

### **Event Publisher**
- Responsável por publicar eventos
- Garante que eventos sejam entregues
- Implementa retry e dead letter queue

### **Event Handler**
- Processa eventos específicos
- Deve ser idempotente
- Pode gerar novos eventos

---

## 📋 Estrutura Base dos Eventos

```kotlin
abstract class DomainEvent {
    val eventId: String = UUID.randomUUID().toString()
    val occurredOn: LocalDateTime = LocalDateTime.now()
    val eventVersion: String = "1.0"
    abstract val aggregateId: String
    abstract val eventType: String
}
```

---

## 🏢 Lead Management Events

### **LeadCreated**
**Descrição**: Disparado quando um novo lead é cadastrado no sistema

**Payload**:
```kotlin
data class LeadCreated(
    override val aggregateId: String, // LeadId
    val leadName: String,
    val phone: String?,
    val email: String?,
    val address: Address,
    val source: LeadSource,
    val createdBy: String
) : DomainEvent() {
    override val eventType = "LeadCreated"
}
```

**Handlers**:
- `Geographic Analytics`: Atualiza densidade territorial
- `Sales Execution`: Cria oportunidades de contato
- `External Integration`: Registra origem do lead

---

### **LeadQualified**
**Descrição**: Lead passou pelo processo de qualificação e foi aprovado

**Payload**:
```kotlin
data class LeadQualified(
    override val aggregateId: String,
    val leadName: String,
    val qualificationCriteria: List<String>,
    val qualifiedBy: String,
    val potentialValue: BigDecimal?
) : DomainEvent() {
    override val eventType = "LeadQualified"
}
```

**Handlers**:
- `Sales Execution`: Prioriza para contatos
- `Geographic Analytics`: Marca como lead qualificado na região

---

### **LeadConverted**
**Descrição**: Lead foi convertido em cliente com sucesso

**Payload**:
```kotlin
data class LeadConverted(
    override val aggregateId: String,
    val leadName: String,
    val clientId: String,
    val conversionDate: LocalDateTime,
    val convertedBy: String,
    val salesCycle: Duration,
    val totalContacts: Int
) : DomainEvent() {
    override val eventType = "LeadConverted"
}
```

**Handlers**:
- `Geographic Analytics`: Atualiza taxa de conversão regional
- `Sales Execution`: Calcula métricas de efetividade
- `External Integration`: Registra sucesso da fonte

---

### **ClientUpdated**
**Descrição**: Informações de cliente foram atualizadas

**Payload**:
```kotlin
data class ClientUpdated(
    override val aggregateId: String,
    val clientName: String,
    val updatedFields: Map<String, Any>,
    val updatedBy: String
) : DomainEvent() {
    override val eventType = "ClientUpdated"
}
```

---

## 📞 Sales Execution Events

### **ContactAttempted**
**Descrição**: Tentativa de contato foi realizada com um lead

**Payload**:
```kotlin
data class ContactAttempted(
    override val aggregateId: String, // ContactLogId
    val leadId: String,
    val contactChannel: ContactChannel,
    val contactResult: ContactResult,
    val scriptUsed: String?,
    val contactedBy: String,
    val nextFollowUp: LocalDate?
) : DomainEvent() {
    override val eventType = "ContactAttempted"
}
```

**Handlers**:
- `Lead Management`: Atualiza histórico de contatos
- `Geographic Analytics`: Registra atividade na região
- `Sales Execution`: Atualiza métricas de script

---

### **ContactSuccessful**
**Descrição**: Contato teve resultado positivo (interessado/agendado/proposta)

**Payload**:
```kotlin
data class ContactSuccessful(
    override val aggregateId: String,
    val leadId: String,
    val contactChannel: ContactChannel,
    val successType: SuccessType, // INTERESTED, MEETING_SCHEDULED, PROPOSAL_REQUESTED
    val scriptUsed: String?,
    val notes: String?
) : DomainEvent() {
    override val eventType = "ContactSuccessful"
}
```

**Handlers**:
- `Lead Management`: Avança status do lead
- `Geographic Analytics`: Registra sucesso regional
- `Sales Execution`: Agenda follow-up automático

---

### **FollowUpScheduled**
**Descrição**: Próximo contato foi agendado

**Payload**:
```kotlin
data class FollowUpScheduled(
    override val aggregateId: String,
    val leadId: String,
    val followUpDate: LocalDate,
    val preferredChannel: ContactChannel,
    val notes: String?,
    val scheduledBy: String
) : DomainEvent() {
    override val eventType = "FollowUpScheduled"
}
```

---

### **ScriptUsed**
**Descrição**: Script foi utilizado em uma tentativa de contato

**Payload**:
```kotlin
data class ScriptUsed(
    override val aggregateId: String, // ScriptId
    val scriptName: String,
    val contactLogId: String,
    val leadId: String,
    val wasSuccessful: Boolean,
    val usedBy: String
) : DomainEvent() {
    override val eventType = "ScriptUsed"
}
```

**Handlers**:
- `Sales Execution`: Atualiza métricas de efetividade
- `Lead Management`: Registra script usado no histórico

---

### **ScriptEffectivenessCalculated**
**Descrição**: Métricas de efetividade do script foram recalculadas

**Payload**:
```kotlin
data class ScriptEffectivenessCalculated(
    override val aggregateId: String,
    val scriptName: String,
    val totalUses: Int,
    val successfulUses: Int,
    val effectivenessRate: Double,
    val calculatedAt: LocalDateTime
) : DomainEvent() {
    override val eventType = "ScriptEffectivenessCalculated"
}
```

---

## 🗺️ Geographic Analytics Events

### **TerritoryAnalyzed**
**Descrição**: Análise territorial foi processada para uma região

**Payload**:
```kotlin
data class TerritoryAnalyzed(
    override val aggregateId: String, // TerritoryId
    val city: String,
    val state: String,
    val totalLeads: Int,
    val qualifiedLeads: Int,
    val convertedLeads: Int,
    val leadDensity: Double,
    val conversionRate: Double,
    val analyzedAt: LocalDateTime
) : DomainEvent() {
    override val eventType = "TerritoryAnalyzed"
}
```

**Handlers**:
- `Lead Management`: Usa dados para priorização
- `Sales Execution`: Ajusta estratégias regionais

---

### **TerritoryPrioritized**
**Descrição**: Território foi marcado como prioritário para ação

**Payload**:
```kotlin
data class TerritoryPrioritized(
    override val aggregateId: String,
    val city: String,
    val state: String,
    val priority: Priority,
    val reason: String,
    val recommendedActions: List<String>
) : DomainEvent() {
    override val eventType = "TerritoryPrioritized"
}
```

**Handlers**:
- `Sales Execution`: Agenda visitas na região
- `Lead Management`: Prioriza leads da região

---

### **DensityCalculated**
**Descrição**: Densidade de leads foi calculada para região

**Payload**:
```kotlin
data class DensityCalculated(
    override val aggregateId: String,
    val region: String,
    val totalLeads: Int,
    val areaKm2: Double,
    val density: Double,
    val densityCategory: DensityCategory // LOW, MEDIUM, HIGH, CRITICAL
) : DomainEvent() {
    override val eventType = "DensityCalculated"
}
```

---

## 🔗 External Integration Events

### **PlacesImported**
**Descrição**: Lote de estabelecimentos foi importado via Google Places

**Payload**:
```kotlin
data class PlacesImported(
    override val aggregateId: String, // ImportBatchId
    val searchQuery: String,
    val searchLocation: String,
    val totalFound: Int,
    val totalImported: Int,
    val importedBy: String,
    val importedLeadIds: List<String>
) : DomainEvent() {
    override val eventType = "PlacesImported"
}
```

**Handlers**:
- `Lead Management`: Processa novos leads importados
- `Geographic Analytics`: Atualiza densidade regional

---

### **ExternalLeadValidated**
**Descrição**: Lead de fonte externa foi validado

**Payload**:
```kotlin
data class ExternalLeadValidated(
    override val aggregateId: String,
    val externalId: String,
    val source: String,
    val validationStatus: ValidationStatus,
    val leadId: String?,
    val validationReason: String?
) : DomainEvent() {
    override val eventType = "ExternalLeadValidated"
}
```

---

### **ImportCompleted**
**Descrição**: Processo de importação foi finalizado

**Payload**:
```kotlin
data class ImportCompleted(
    override val aggregateId: String,
    val importType: ImportType,
    val totalProcessed: Int,
    val successCount: Int,
    val errorCount: Int,
    val completedAt: LocalDateTime,
    val errors: List<String>
) : DomainEvent() {
    override val eventType = "ImportCompleted"
}
```

---

## 🔄 Event Flow Diagrams

### **Fluxo de Criação de Lead**
```
LeadCreated (Lead Management)
    ↓
    ├── → Geographic Analytics: DensityCalculated
    ├── → Sales Execution: ContactOpportunityCreated
    └── → External Integration: SourceRegistered
```

### **Fluxo de Contato Bem-sucedido**
```
ContactSuccessful (Sales Execution)
    ↓
    ├── → Lead Management: LeadStatusUpdated
    ├── → Sales Execution: FollowUpScheduled
    └── → Geographic Analytics: RegionalActivityRecorded
```

### **Fluxo de Conversão**
```
LeadConverted (Lead Management)
    ↓
    ├── → Geographic Analytics: ConversionRecorded
    ├── → Sales Execution: ScriptEffectivenessCalculated
    └── → External Integration: SourceSuccessRecorded
```

---

## 📊 Event Monitoring

### **Métricas de Eventos**
- Volume de eventos por tipo
- Latência de processamento
- Taxa de erro nos handlers
- Eventos em dead letter queue

### **Health Checks**
- Event store disponibilidade
- Publishers funcionando
- Handlers processando
- Filas sem backlog excessivo

---

## 🛠️ Implementação Técnica

### **Event Store**
- Armazena todos os eventos em ordem cronológica
- Garante durabilidade e auditoria
- Permite replay de eventos

### **Message Bus**
- Kafka para eventos entre bounded contexts
- In-memory para eventos dentro do mesmo contexto
- Garantia de entrega at-least-once

### **Event Handlers**
- Implementam padrão Command Handler
- São idempotentes por design
- Processam eventos de forma assíncrona

---

## 🔒 Versionamento de Eventos

### **Estratégias de Evolução**
- **Aditiva**: Novos campos opcionais
- **Transformação**: Migração automática
- **Múltiplas Versões**: Suporte a versões antigas

### **Schema Evolution**
```kotlin
// Versão 1.0
data class LeadCreatedV1(...)

// Versão 2.0 - Adicionou campo opcional
data class LeadCreatedV2(
    // ... campos da V1
    val estimatedValue: BigDecimal? = null
)
```

---

**Nota**: Os eventos de domínio são fundamentais para a comunicação entre bounded contexts. Eles devem ser projetados cuidadosamente, pois representam contratos entre contextos e sua mudança pode impactar múltiplas partes do sistema. 