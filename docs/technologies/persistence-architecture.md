# 🏗️ Arquitetura de Persistência - Ports & Adapters

## 📖 Visão Geral

Este documento explica como a camada de persistência está estruturada seguindo o padrão **Ports & Adapters** (Arquitetura Hexagonal).

## 🎯 Objetivo da Arquitetura Hexagonal

A Arquitetura Hexagonal tem como objetivo **isolar o domínio** (regras de negócio) de detalhes técnicos como:
- Framework de persistência (Spring Data JDBC, JPA, MongoDB, etc)
- Banco de dados (PostgreSQL, MySQL, etc)
- APIs externas
- Frameworks web

## 📦 Estrutura Atual - Lead Aggregate

```
src/main/kotlin/br/com/gustavoantunes/bridalcovercrm/

├── domain/                                    # ⚡ NÚCLEO - Regras de Negócio
│   └── model/
│       └── lead/
│           ├── Lead.kt                        # Aggregate Root (Rich Domain Model)
│           ├── LeadId.kt                      # Value Object
│           ├── LeadStatus.kt                  # Enum com lógica de negócio
│           └── LeadSource.kt                  # Enum com lógica de negócio
│
├── application/                               # 🔌 PORTAS (Interfaces)
│   └── port/
│       ├── in/                                # Input Ports (Use Cases)
│       │   └── lead/
│       │       └── RegisterLeadUseCase.kt     # Interface do caso de uso
│       │
│       └── out/                               # Output Ports (SPI)
│           └── repository/
│               └── LeadRepository.kt          # ⭐ PORT - Interface em termos de DOMÍNIO
│
└── infrastructure/                            # 🔧 ADAPTADORES (Implementações)
    └── persistence/
        ├── adapter/                           # ⭐ ADAPTER - Implementa o Port
        │   └── LeadRepositoryAdapter.kt       # Conecta Port com Data Repository
        │
        ├── repository/                        # Spring Data Repository
        │   └── LeadDataJdbcRepository.kt      # CrudRepository<LeadEntity>
        │
        ├── entity/                            # Modelo de dados (tabela)
        │   └── LeadEntity.kt                  # Data class para persistência
        │
        └── mapper/                            # Conversores
            ├── LeadMapper.kt                  # Lead ↔ LeadEntity
            └── ContactInfoJsonMapper.kt       # ContactInfo ↔ JSON
```

## 🔄 Fluxo de Dados - Como Tudo se Conecta

### 1️⃣ Salvando um Lead (Write Flow)

```
[Service] → [Port Interface] → [Adapter] → [Mapper] → [Spring Data] → [Database]
   ↓            ↓                 ↓           ↓            ↓              ↓
RegisterLead   Lead          Lead →      Lead →       LeadEntity    INSERT INTO
UseCase      Repository    Adapter     toEntity()    JDBC Repo      leads...
```

**Passo a passo:**

```kotlin
// 1. Service chama o PORT (interface de domínio)
class RegisterLeadService(
    private val leadRepository: LeadRepository  // ← Depende da INTERFACE
) {
    fun execute(command: RegisterLeadCommand): Lead {
        val lead = Lead(...)  // Cria aggregate de domínio
        return leadRepository.save(lead)  // ← Usa termos de DOMÍNIO (Lead)
    }
}

// 2. ADAPTER implementa o PORT
@Component
class LeadRepositoryAdapter(
    private val dataRepository: LeadDataJdbcRepository  // ← Spring Data
) : LeadRepository {  // ← Implementa o PORT
    
    override fun save(lead: Lead): Lead {
        // 3. MAPPER converte Domain → Entity
        val entity = LeadMapper.toEntity(lead)
        
        // 4. Salva usando Spring Data
        val savedEntity = dataRepository.save(entity)
        
        // 5. MAPPER converte Entity → Domain
        return LeadMapper.toDomain(savedEntity)
    }
}

// 3. MAPPER faz a tradução entre camadas
object LeadMapper {
    fun toEntity(lead: Lead): LeadEntity {
        return LeadEntity(
            id = lead.id.value,              // LeadId → String
            companyName = lead.name.value,   // CompanyName → String
            cnpj = lead.cnpj?.getDigits(),   // CNPJ → String
            contactInfo = ContactInfoJsonMapper.toJson(lead.contactInfo),  // ← JSON
            status = lead.status.name,       // LeadStatus → String
            source = lead.source.name,       // LeadSource → String
            createdAt = lead.createdAt,
            updatedAt = lead.updatedAt
        )
    }
}

// 4. Spring Data Repository (interface do Spring)
@Repository
interface LeadDataJdbcRepository : CrudRepository<LeadEntity, String>

// 5. Entity (representação da tabela)
@Table("leads")
data class LeadEntity(
    @Id val id: String,
    @Column("company_name") val companyName: String,
    @Column("cnpj") val cnpj: String?,
    @Column("contact_info") val contactInfo: String,  // ← JSON
    @Column("status") val status: String,
    @Column("source") val source: String,
    @Column("created_at") val createdAt: LocalDateTime,
    @Column("updated_at") val updatedAt: LocalDateTime
)
```

### 2️⃣ Buscando um Lead (Read Flow)

```
[Service] → [Port Interface] → [Adapter] → [Spring Data] → [Database]
                                    ↓           ↓              ↓
                                [Mapper]    LeadEntity    SELECT * FROM
                                    ↓           ↓           leads...
                                 Lead ← toDomain(entity)
```

## 🤔 Por Que Cada Camada Existe?

### 1. **Domain Model (Lead, LeadId, etc)** - O Coração
- ✅ Contém as **regras de negócio**
- ✅ **Independente** de frameworks e banco de dados
- ✅ Usa **Value Objects** para garantir invariantes
- ✅ Pode ser testado **sem infraestrutura**

### 2. **Port (LeadRepository interface)** - O Contrato
- ✅ Define **O QUE** a aplicação precisa (não COMO)
- ✅ Usa **linguagem de domínio** (Lead, LeadId)
- ✅ Permite **trocar a implementação** sem alterar o domínio
- ✅ Facilita **testes com mocks**

```kotlin
interface LeadRepository {
    fun save(lead: Lead): Lead         // ← Termos de DOMÍNIO
    fun findById(id: LeadId): Lead?    // ← Não menciona banco, tabela, etc
}
```

### 3. **Adapter (LeadRepositoryAdapter)** - A Ponte
- ✅ **Implementa o Port** (contrato)
- ✅ Faz a **tradução** entre domínio e infraestrutura
- ✅ Orquestra **Mapper + Spring Data Repository**
- ✅ Contém a **lógica de adaptação**

### 4. **Mapper (LeadMapper, ContactInfoJsonMapper)** - O Tradutor
- ✅ Converte **Lead ↔ LeadEntity**
- ✅ Converte **Value Objects ↔ tipos primitivos**
- ✅ Serializa **ContactInfo ↔ JSON**
- ✅ Mantém o **domínio puro**

### 5. **Entity (LeadEntity)** - O Modelo de Dados
- ✅ Representa a **estrutura da tabela**
- ✅ Usa anotações do **Spring Data JDBC**
- ✅ **Simples e anêmica** (sem lógica de negócio)
- ✅ Otimizada para **persistência**

### 6. **Spring Data Repository (LeadDataJdbcRepository)** - O Acesso a Dados
- ✅ Fornecido pelo **Spring Data**
- ✅ Operações CRUD **automáticas**
- ✅ Trabalha com **LeadEntity** (não Lead)

## ⚖️ Comparação: Domain vs Entity

| Aspecto | Domain Model (Lead) | Entity (LeadEntity) |
|---------|---------------------|---------------------|
| **Propósito** | Regras de negócio | Persistência |
| **Complexidade** | Rica (comportamentos) | Anêmica (dados) |
| **Tipos** | Value Objects (LeadId, CNPJ) | Primitivos (String) |
| **Validações** | Sim, no construtor | Não |
| **Anotações** | Nenhuma | Spring Data JDBC |
| **Mutabilidade** | Controlada | Imutável (data class) |

### Exemplo Prático:

```kotlin
// ❌ DOMAIN MODEL - Rico em comportamento
class Lead(
    override val id: LeadId,              // Value Object
    var name: CompanyName,                // Value Object com validação
    var cnpj: CNPJ? = null,               // Value Object com validação de CNPJ
    var contactInfo: ContactInfo,         // Value Object complexo
    var status: LeadStatus = LeadStatus.NEW,  // Enum com lógica
    val source: LeadSource,               // Enum com lógica
    // ...
) {
    init {
        // ✅ Valida invariantes no construtor
        require(!createdAt.isAfter(LocalDateTime.now().plusMinutes(1))) { 
            "Creation date cannot be in the future" 
        }
        require(!updatedAt.isBefore(createdAt)) { 
            "Update date cannot be before creation date" 
        }
        require(!(source == LeadSource.WEBSITE && !contactInfo.hasEmail())) { 
            "Leads from website must have email" 
        }
    }
}

// ❌ ENTITY - Simples e focada em persistência
@Table("leads")
data class LeadEntity(
    @Id val id: String,                   // String simples
    @Column("company_name") val companyName: String,  // String simples
    @Column("cnpj") val cnpj: String?,    // String (sem validação)
    @Column("contact_info") val contactInfo: String,  // JSON
    @Column("status") val status: String, // String
    // ...
)  // ← Sem validações, sem lógica
```

## 🎨 Diagrama Visual

```
┌─────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                            │
│  ┌────────────────────┐            ┌──────────────────────────┐    │
│  │ RegisterLeadService │───────────▶│  LeadRepository (PORT)   │    │
│  │  (Use Case)         │            │  ┌─────────────────────┐ │    │
│  └────────────────────────┘            │  + save(Lead): Lead  │ │    │
│                                        │  + findById(): Lead? │ │    │
│                                        └─────────────────────┘ │    │
└─────────────────────────────────────────────┬───────────────────────┘
                                               │ implements
                                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                            │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │           LeadRepositoryAdapter (ADAPTER)                   │    │
│  │  ┌──────────────────────────────────────────────────────┐  │    │
│  │  │ override fun save(lead: Lead): Lead {                │  │    │
│  │  │   val entity = LeadMapper.toEntity(lead)        ←────┼──┼─┐  │
│  │  │   val saved = dataRepository.save(entity)       ←────┼──┼─┼─┐│
│  │  │   return LeadMapper.toDomain(saved)             ←────┼──┼─┘ ││
│  │  │ }                                                      │  │   ││
│  │  └──────────────────────────────────────────────────────┘  │   ││
│  └────────────────────────────────────────────────────────────┘   ││
│         │                        │                                 ││
│         │ usa                    │ usa                             ││
│         ▼                        ▼                                 ││
│  ┌─────────────┐        ┌───────────────────────┐                ││
│  │ LeadMapper  │        │ LeadDataJdbcRepository│◀───────────────┘│
│  ├─────────────┤        │  (Spring Data)        │                 │
│  │ toEntity()  │        └───────────────────────┘                 │
│  │ toDomain()  │                 │                                 │
│  └─────────────┘                 │ persiste                        │
│         │                        ▼                                 │
│         │ converte      ┌─────────────────┐                        │
│         └──────────────▶│   LeadEntity    │                        │
│                         │   (@Table)      │                        │
│                         └─────────────────┘                        │
│                                  │                                  │
└──────────────────────────────────┼──────────────────────────────────┘
                                   │
                                   ▼
                          ┌────────────────┐
                          │   PostgreSQL   │
                          │  (leads table) │
                          └────────────────┘
```

## ✅ Vantagens Dessa Estrutura

### 1. **Domínio Isolado**
```kotlin
// ✅ Lead não sabe nada sobre banco de dados
class Lead(...) {
    // Sem @Table, @Column, @Id, etc
}
```

### 2. **Fácil Trocar Implementação**
```kotlin
// Quer trocar Spring Data JDBC por JPA?
// → Só precisa modificar: Adapter, Entity e Mapper
// → Port e Domain continuam iguais! ✅

// Quer usar MongoDB?
// → Mesmo Port (LeadRepository)
// → Novo Adapter (LeadRepositoryMongoAdapter)
// → Nova Entity (LeadDocument)
// → Service não muda NADA! ✅
```

### 3. **Testes Facilitados**
```kotlin
class RegisterLeadServiceTest {
    @Test
    fun `should register lead`() {
        // Mock do PORT (interface)
        val mockRepository = mock<LeadRepository>()
        val service = RegisterLeadService(mockRepository)
        
        // ✅ Testa sem banco de dados!
        // ✅ Testa sem Spring!
        // ✅ Testa apenas lógica de negócio!
    }
}
```

### 4. **Regras de Negócio Centralizadas**
```kotlin
// ✅ Validações no Domain Model
class Lead(...) {
    init {
        require(!(source == LeadSource.WEBSITE && !contactInfo.hasEmail())) {
            "Leads from website must have email"
        }
    }
}

// ❌ NÃO na Entity
@Table("leads")
data class LeadEntity(...)  // Sem validações
```

## 🔍 Quando Usar Cada Componente

| Situação | Use |
|----------|-----|
| Adicionar validação de negócio | **Domain Model** |
| Adicionar novo campo no banco | **Entity + Mapper** |
| Mudar banco de dados | **Adapter + Entity + Mapper** |
| Adicionar nova query | **Port + Adapter** |
| Adicionar caso de uso | **Use Case + Service** |

## 📚 Resumo da Separação

| Camada | Responsabilidade | Conhece |
|--------|------------------|---------|
| **Domain** | Regras de negócio | Nada de infra |
| **Port** | Contrato | Domain |
| **Adapter** | Implementação | Port + Infra |
| **Mapper** | Tradução | Domain + Entity |
| **Entity** | Estrutura de dados | Banco |
| **Spring Data** | CRUD automático | Entity |

## 🎯 Conclusão

A estrutura atual segue **corretamente** o padrão Ports & Adapters:

1. ✅ **Port** (LeadRepository) define o contrato em termos de domínio
2. ✅ **Adapter** (LeadRepositoryAdapter) implementa o contrato
3. ✅ **Mapper** traduz entre Domain e Entity
4. ✅ **Entity** representa a tabela
5. ✅ **Domain Model** permanece puro e independente

Esta separação garante:
- 🔒 **Domínio protegido** de detalhes técnicos
- 🔄 **Flexibilidade** para trocar tecnologias
- ✅ **Testabilidade** sem dependências externas
- 📦 **Coesão** e baixo acoplamento

