# 🎯 Estrutura de Persistência - Guia Rápido

## 📁 Estrutura Atual

```
src/main/kotlin/br/com/gustavoantunes/bridalcovercrm/

├── domain/                                    # ⚡ NÚCLEO - Regras de Negócio
│   └── model/
│       ├── common/                            # Building blocks do DDD
│       │   ├── AggregateRoot.kt
│       │   ├── AggregateId.kt
│       │   ├── DomainEvent.kt
│       │   └── DomainObject.kt
│       │
│       ├── lead/                              # Lead Aggregate
│       │   ├── Lead.kt                        # ⭐ Aggregate Root (Rico)
│       │   ├── LeadId.kt                      # Value Object
│       │   ├── LeadStatus.kt                  # Enum com lógica
│       │   └── LeadSource.kt                  # Enum com lógica
│       │
│       └── shared/                            # Value Objects compartilhados
│           ├── CompanyName.kt                 # Nome da empresa
│           ├── CNPJ.kt                        # CNPJ validado
│           ├── ContactInfo.kt                 # Informações de contato
│           ├── Email.kt                       # Email validado
│           ├── Phone.kt                       # Telefone validado
│           └── SocialMediaType.kt             # Tipos de mídia social
│
├── application/                               # 🔌 PORTAS (Interfaces)
│   ├── port/
│   │   ├── in/                                # Input Ports (Use Cases)
│   │   │   └── lead/
│   │   │       ├── RegisterLeadUseCase.kt     # Interface do caso de uso
│   │   │       └── GetLeadUseCase.kt          # Interface do caso de uso
│   │   │
│   │   └── out/                               # Output Ports (SPI)
│   │       └── repository/
│   │           └── LeadRepository.kt          # ⭐ PORT - Interface em termos de DOMÍNIO
│   │
│   └── service/                               # Implementação dos Use Cases
│       └── lead/
│           ├── RegisterLeadService.kt         # Implementa RegisterLeadUseCase
│           └── GetLeadService.kt              # Implementa GetLeadUseCase
│
└── infrastructure/                            # 🔧 ADAPTADORES (Implementações)
    └── persistence/                           # Camada de Persistência
        ├── adapter/                           # ⭐ ADAPTERS - Implementam os Ports
        │   └── LeadRepositoryAdapter.kt       # Implementa LeadRepository
        │
        ├── repository/                        # Spring Data Repositories
        │   └── LeadDataJdbcRepository.kt      # CrudRepository<LeadEntity>
        │
        ├── entity/                            # Modelos de dados (tabelas)
        │   └── LeadEntity.kt                  # Data class para persistência
        │
        └── mapper/                            # Conversores
            ├── LeadMapper.kt                  # Lead ↔ LeadEntity
            └── ContactInfoJsonMapper.kt       # ContactInfo ↔ JSON
```

## 🎭 Os 6 Atores Principais

### 1️⃣ Domain Model (Lead.kt)
**O QUE É:** O coração do sistema - contém as regras de negócio

```kotlin
class Lead(
    override val id: LeadId,              // Value Object (não String)
    var name: CompanyName,                // Value Object com validação
    var cnpj: CNPJ? = null,               // Value Object com validação CNPJ
    var contactInfo: ContactInfo,         // Value Object complexo
    var status: LeadStatus,               // Enum com comportamento
    val source: LeadSource,               // Enum com comportamento
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) : AggregateRootWithId<LeadId>() {
    init {
        // ✅ Validações e invariantes aqui
        require(!createdAt.isAfter(LocalDateTime.now().plusMinutes(1)))
        require(!(source == LeadSource.WEBSITE && !contactInfo.hasEmail()))
    }
}
```

**RESPONSABILIDADE:**
- ✅ Garantir regras de negócio
- ✅ Manter invariantes
- ✅ Usar Value Objects
- ❌ NUNCA conhecer banco de dados, Spring, etc

---

### 2️⃣ Port Interface (LeadRepository.kt)
**O QUE É:** Contrato definido pela aplicação

```kotlin
interface LeadRepository {
    fun save(lead: Lead): Lead         // ← Usa tipos de DOMÍNIO
    fun findById(id: LeadId): Lead?    // ← Não menciona Entity, tabela, etc
}
```

**RESPONSABILIDADE:**
- ✅ Definir **O QUE** a aplicação precisa
- ✅ Usar linguagem de domínio (Lead, LeadId)
- ✅ Permitir múltiplas implementações
- ❌ NÃO definir COMO implementar

**VANTAGEM:** 
- Service depende da **interface** (não da implementação)
- Fácil fazer mocks para testes
- Fácil trocar implementação (JDBC → JPA → MongoDB)

---

### 3️⃣ Adapter (LeadRepositoryAdapter.kt)
**O QUE É:** A ponte entre domínio e infraestrutura

```kotlin
@Component
class LeadRepositoryAdapter(
    private val dataRepository: LeadDataJdbcRepository  // Spring Data
) : LeadRepository {  // Implementa o Port
    
    override fun save(lead: Lead): Lead {
        // 1. Converte Domain → Entity
        val entity = LeadMapper.toEntity(lead)
        
        // 2. Salva usando Spring Data
        val savedEntity = dataRepository.save(entity)
        
        // 3. Converte Entity → Domain
        return LeadMapper.toDomain(savedEntity)
    }
    
    override fun findById(id: LeadId): Lead? {
        return dataRepository.findById(id.value)
            .map { LeadMapper.toDomain(it) }
            .orElse(null)
    }
}
```

**RESPONSABILIDADE:**
- ✅ Implementar o Port
- ✅ Orquestrar Mapper + Spring Data
- ✅ Traduzir exceções de infra para domínio
- ✅ Fazer transações (se necessário)

**POR QUE EXISTE?**
- Service trabalha com **Lead** (domínio)
- Spring Data trabalha com **LeadEntity** (infra)
- Adapter faz a **tradução**

---

### 4️⃣ Mapper (LeadMapper.kt)
**O QUE É:** Tradutor entre domínio e persistência

```kotlin
object LeadMapper {
    fun toEntity(lead: Lead): LeadEntity {
        return LeadEntity(
            id = lead.id.value,                    // LeadId → String
            companyName = lead.name.value,         // CompanyName → String
            cnpj = lead.cnpj?.getDigits(),         // CNPJ → String (14 dígitos)
            contactInfo = ContactInfoJsonMapper.toJson(lead.contactInfo),  // → JSON
            status = lead.status.name,             // LeadStatus → String
            source = lead.source.name,             // LeadSource → String
            createdAt = lead.createdAt,
            updatedAt = lead.updatedAt
        )
    }
    
    fun toDomain(entity: LeadEntity): Lead {
        return Lead(
            id = LeadId.fromString(entity.id),              // String → LeadId
            name = CompanyName(entity.companyName),         // String → CompanyName
            cnpj = entity.cnpj?.let { CNPJ.fromString(it) }, // String → CNPJ
            contactInfo = ContactInfoJsonMapper.fromJson(entity.contactInfo), // JSON → ContactInfo
            status = LeadStatus.valueOf(entity.status),     // String → LeadStatus
            source = LeadSource.valueOf(entity.source),     // String → LeadSource
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
```

**RESPONSABILIDADE:**
- ✅ Converter **Lead** ↔ **LeadEntity**
- ✅ Converter **Value Objects** ↔ **primitivos**
- ✅ Serializar **ContactInfo** ↔ **JSON**
- ✅ Manter domínio puro

**POR QUE EXISTE?**
- Domínio usa **Value Objects** (LeadId, CNPJ, CompanyName)
- Banco usa **primitivos** (String, JSON)
- Mapper faz a **conversão**

---

### 5️⃣ Entity (LeadEntity.kt)
**O QUE É:** Representação da tabela do banco

```kotlin
@Table("leads")
data class LeadEntity(
    @Id 
    val id: String,                           // String simples
    
    @Column("company_name") 
    val companyName: String,                  // String simples
    
    @Column("cnpj") 
    val cnpj: String?,                        // String (sem validação)
    
    @Column("contact_info") 
    val contactInfo: String,                  // JSON como String
    
    @Column("status") 
    val status: String,                       // String (não enum)
    
    @Column("source") 
    val source: String,                       // String (não enum)
    
    @Column("created_at") 
    val createdAt: LocalDateTime,
    
    @Column("updated_at") 
    val updatedAt: LocalDateTime
)
```

**RESPONSABILIDADE:**
- ✅ Mapear campos da tabela
- ✅ Usar tipos primitivos
- ✅ Anotações do Spring Data
- ❌ SEM validações
- ❌ SEM lógica de negócio

**POR QUE É DIFERENTE DO DOMAIN?**
- **Domain** = Rico, com validações e comportamento
- **Entity** = Anêmico, apenas dados

---

### 6️⃣ Spring Data Repository (LeadDataJdbcRepository.kt)
**O QUE É:** Interface do Spring que gera CRUD automaticamente

```kotlin
@Repository
interface LeadDataJdbcRepository : CrudRepository<LeadEntity, String>
```

**RESPONSABILIDADE:**
- ✅ CRUD automático
- ✅ Queries geradas pelo Spring
- ✅ Trabalha com **LeadEntity**

**O QUE GANHA DE GRAÇA:**
- `save(entity)` → INSERT/UPDATE
- `findById(id)` → SELECT por ID
- `findAll()` → SELECT *
- `deleteById(id)` → DELETE
- E muito mais...

---

## 🔄 Fluxo Completo: Salvando um Lead

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. SERVICE (Application Layer)                                  │
│    RegisterLeadService recebe comando                           │
│    Cria Lead (domain) com validações                            │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. PORT (Interface)                                             │
│    leadRepository.save(lead)  ← Usa tipos de DOMÍNIO           │
└─────────────────────────────────────────────────────────────────┘
                            │ implements
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. ADAPTER (Infrastructure)                                     │
│    LeadRepositoryAdapter.save(lead)                             │
│    ├─ LeadMapper.toEntity(lead)     → LeadEntity               │
│    ├─ dataRepository.save(entity)   → Salva no banco           │
│    └─ LeadMapper.toDomain(saved)    → Lead                     │
└─────────────────────────────────────────────────────────────────┘
                            │
                    ┌───────┴───────┐
                    ▼               ▼
    ┌──────────────────┐   ┌──────────────────┐
    │ 4. MAPPER        │   │ 5. SPRING DATA   │
    │ Lead → Entity    │   │ save(entity)     │
    │ Entity → Lead    │   │ findById(id)     │
    └──────────────────┘   └──────────────────┘
                                    │
                                    ▼
                    ┌──────────────────────────┐
                    │ 6. POSTGRESQL            │
                    │ INSERT INTO leads (...)  │
                    └──────────────────────────┘
```

## ✅ Por Que Essa Separação?

### 🎯 Isolar o Domínio
```kotlin
// ✅ BOM: Domain puro
class Lead(...) {
    // Sem @Table, @Column, @Entity
    // Sem dependências de Spring
    // Apenas lógica de negócio
}

// ❌ RUIM: Domain acoplado
@Entity  // ← Anotação do JPA/Spring
@Table("leads")
class Lead(...) {
    @Id
    @Column("id")
    var id: String  // ← String ao invés de LeadId
    
    // Mistura regras de negócio com infra
}
```

### 🔄 Trocar Tecnologia Facilmente
```kotlin
// Quer trocar Spring Data JDBC por JPA?
// → Muda: Entity, Adapter, Mapper
// → NÃO muda: Domain, Port, Service ✅

// Quer trocar PostgreSQL por MongoDB?
// → Cria: LeadDocument, LeadRepositoryMongoAdapter
// → NÃO muda: Domain, Port, Service ✅
```

### ✅ Testar Sem Banco
```kotlin
@Test
fun `should register lead`() {
    // Mock do PORT (interface)
    val mockRepository = mock<LeadRepository>()
    whenever(mockRepository.save(any())).thenReturn(lead)
    
    val service = RegisterLeadService(mockRepository)
    
    // ✅ Testa sem banco!
    // ✅ Testa sem Spring!
    // ✅ Rápido e confiável!
}
```

### 📦 Baixo Acoplamento
```
Domain ← Application → Infrastructure

- Domain não conhece nada
- Application define contratos (Ports)
- Infrastructure implementa (Adapters)
```

## 🎯 Resumo: Quem Faz O Quê?

| Componente | Responsabilidade | Conhece |
|------------|------------------|---------|
| **Lead** | Regras de negócio | Nada de infra |
| **LeadRepository (Port)** | Contrato | Domain |
| **LeadRepositoryAdapter** | Implementação | Port + Infra |
| **LeadMapper** | Conversão | Domain + Entity |
| **LeadEntity** | Estrutura da tabela | Banco |
| **LeadDataJdbcRepository** | CRUD automático | Entity |

## 🚀 Próximos Passos

Quando precisar adicionar novos recursos:

### Adicionar nova query?
1. Adicione método no **Port** (LeadRepository)
2. Implemente no **Adapter** (LeadRepositoryAdapter)
3. Use **Mapper** se necessário
4. Crie query no **Spring Data Repository** se necessário

### Adicionar novo campo?
1. Adicione no **Domain Model** (Lead)
2. Adicione na **Entity** (LeadEntity)
3. Atualize o **Mapper** (LeadMapper)
4. Crie **migration** no banco

### Trocar tecnologia?
1. Crie nova **Entity**
2. Crie novo **Adapter**
3. Atualize **Mapper**
4. **Domain** e **Port** permanecem iguais! ✅

---

## 📚 Documentação Adicional

- [Arquitetura de Persistência Detalhada](./persistence-architecture.md)
- [Diagrama de Fluxo](../models/persistence-flow.puml)
- [Diagrama de Sequência](../models/persistence-sequence.puml)

