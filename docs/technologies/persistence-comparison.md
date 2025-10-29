# 🗄️ Comparação de Tecnologias de Persistência

## 📋 Visão Geral

Este documento compara três abordagens de persistência no Spring Framework: **Spring JDBC**, **Spring Data JDBC** e **Spring Data JPA**. Cada uma tem suas vantagens e desvantagens, sendo adequada para diferentes cenários.

---

## 🔍 **1. Spring JDBC (Manual)**

### **Características**
- **Controle total** sobre SQL
- **Mapeamento manual** ResultSet ↔ Objects
- **Performance máxima** (sem overhead)
- **Flexibilidade total** para queries complexas

### **Implementação**
```kotlin
@Repository
class LeadJdbcRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val rowMapper: LeadRowMapper,
    private val parameterMapper: LeadParameterMapper
) : LeadRepository {

    override fun save(lead: Lead): Lead {
        val parameters = parameterMapper.mapToParameters(lead)
        val existingLead = findById(lead.id)
        
        if (existingLead != null) {
            jdbcTemplate.update(UPDATE_SQL, parameters)
        } else {
            jdbcTemplate.update(INSERT_SQL, parameters)
        }
        
        return findById(lead.id)!!
    }

    override fun findById(id: LeadId): Lead? {
        return try {
            val parameters = MapSqlParameterSource("id", id.value)
            jdbcTemplate.queryForObject(FIND_BY_ID_SQL, parameters, rowMapper)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    companion object {
        private const val INSERT_SQL = """
            INSERT INTO leads (id, company_name, cnpj, contact_info, status, source, created_at, updated_at)
            VALUES (:id, :company_name, :cnpj, :contact_info::jsonb, :status, :source, :created_at, :updated_at)
        """
        
        private const val UPDATE_SQL = """
            UPDATE leads 
            SET company_name = :company_name, cnpj = :cnpj, contact_info = :contact_info::jsonb, 
                status = :status, source = :source, updated_at = :updated_at
            WHERE id = :id
        """
        
        private const val FIND_BY_ID_SQL = "SELECT * FROM leads WHERE id = :id"
    }
}

// Row Mapper manual
@Component
class LeadRowMapper : RowMapper<Lead> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Lead {
        return Lead(
            id = LeadId.fromString(rs.getString("id")),
            name = CompanyName(rs.getString("company_name")),
            cnpj = rs.getString("cnpj")?.let { CNPJ.fromString(it) },
            // ... mapeamento manual de todos os campos
        )
    }
}
```

### **Vantagens**
- ✅ **Performance máxima** (100%)
- ✅ **Controle total** sobre SQL
- ✅ **Queries complexas** sem limitações
- ✅ **Debugging fácil** (SQL visível)
- ✅ **Otimizações específicas** possíveis

### **Desvantagens**
- ❌ **Muito código** para escrever (~200 linhas)
- ❌ **Mapeamento manual** (propenso a erros)
- ❌ **Baixa produtividade**
- ❌ **Manutenção complexa**

### **Quando usar**
- Projetos com **requisitos extremos de performance**
- **Queries muito complexas** e específicas
- Equipe com **expertise avançada em SQL**
- **Otimizações críticas** de banco de dados

---

## 🚀 **2. Spring Data JDBC (Escolha Atual)**

### **Características**
- **Métodos automáticos** para operações básicas
- **SQL nativo** para queries customizadas
- **Sem overhead de ORM**
- **Meio termo** entre controle e produtividade

### **Implementação**
```kotlin
// Entity simples com annotations básicas
@Table("leads")
data class LeadEntity(
    @Id
    val id: String,
    
    @Column("company_name")
    val companyName: String,
    
    val cnpj: String?,
    
    @Column("contact_info")
    val contactInfoJson: String,
    
    val status: String,
    val source: String,
    
    @Column("created_at")
    val createdAt: LocalDateTime,
    
    @Column("updated_at")
    val updatedAt: LocalDateTime
) {
    fun toDomain(): Lead = Lead(...)
    
    companion object {
        fun fromDomain(lead: Lead): LeadEntity = LeadEntity(...)
    }
}

// Repository com métodos automáticos + SQL customizado
@Repository
interface LeadDataJdbcRepository : CrudRepository<LeadEntity, String> {
    
    // Métodos automáticos disponíveis:
    // - save(entity)
    // - findById(id)
    // - findAll()
    // - deleteById(id)
    // - count()
    // - existsById(id)
    
    @Query("SELECT * FROM leads WHERE status = :status")
    fun findByStatus(@Param("status") status: String): List<LeadEntity>
    
    @Query("SELECT * FROM leads WHERE status NOT IN ('CONVERTED', 'LOST')")
    fun findActiveLeads(): List<LeadEntity>
    
    @Query("""
        SELECT * FROM leads 
        WHERE (:companyName IS NULL OR LOWER(company_name) LIKE LOWER(:companyName))
        AND (:status IS NULL OR status = :status)
        ORDER BY created_at DESC
        LIMIT :size OFFSET :offset
    """)
    fun searchLeads(
        @Param("companyName") companyName: String?,
        @Param("status") status: String?,
        @Param("size") size: Int,
        @Param("offset") offset: Int
    ): List<LeadEntity>
}

// Implementação do repositório de domínio
@Repository
class LeadRepositoryImpl(
    private val dataJdbcRepository: LeadDataJdbcRepository
) : LeadRepository {

    override fun save(lead: Lead): Lead {
        val entity = LeadEntity.fromDomain(lead)
        val savedEntity = dataJdbcRepository.save(entity) // ← Automático!
        return savedEntity.toDomain()
    }

    override fun findById(id: LeadId): Lead? {
        return dataJdbcRepository.findById(id.value) // ← Automático!
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByStatus(status: LeadStatus): List<Lead> {
        return dataJdbcRepository.findByStatus(status.name) // ← SQL customizado
            .map { it.toDomain() }
    }
}
```

### **Vantagens**
- ✅ **Métodos automáticos** (save, findById, delete, count)
- ✅ **SQL nativo** para queries complexas
- ✅ **Performance alta** (95% do JDBC manual)
- ✅ **Menos código** (60% redução)
- ✅ **Manutenibilidade** boa
- ✅ **Produtividade** alta

### **Desvantagens**
- ❌ **Relacionamentos limitados** (sem lazy loading)
- ❌ **Sem cache automático**
- ❌ **Paginação manual** para queries customizadas

### **Quando usar**
- Projetos que precisam de **boa performance** + **produtividade**
- **Queries moderadamente complexas**
- Equipe com **conhecimento médio de SQL**
- **Aplicações CRUD** com algumas otimizações

---

## 🏢 **3. Spring Data JPA (Hibernate)**

### **Características**
- **ORM completo** com mapeamento automático
- **Relacionamentos complexos** automáticos
- **Cache de segundo nível**
- **Máxima produtividade** para CRUD

### **Implementação**
```kotlin
// Entity JPA com annotations complexas
@Entity
@Table(name = "leads")
class LeadJpaEntity(
    @Id
    @Column(name = "id")
    var id: String,
    
    @Column(name = "company_name", nullable = false, length = 200)
    var companyName: String,
    
    @Column(name = "cnpj", length = 14)
    var cnpj: String?,
    
    @Column(name = "contact_info", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = ContactInfoConverter::class)
    var contactInfo: ContactInfo,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: LeadStatus,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    var source: LeadSource,
    
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime,
    
    // Relacionamentos automáticos
    @OneToMany(mappedBy = "lead", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var contactAttempts: MutableList<ContactAttemptEntity> = mutableListOf()
) {
    fun toDomain(): Lead = Lead(...)
    
    companion object {
        fun fromDomain(lead: Lead): LeadJpaEntity = LeadJpaEntity(...)
    }
}

// Repository JPA - muito menos código!
@Repository
interface LeadJpaRepository : JpaRepository<LeadJpaEntity, String>, JpaSpecificationExecutor<LeadJpaEntity> {
    
    // Métodos automáticos: save(), findById(), delete(), findAll(), etc.
    
    fun findByStatus(status: LeadStatus): List<LeadJpaEntity>
    
    fun findByStatusIn(statuses: List<LeadStatus>): List<LeadJpaEntity>
    
    fun findByCompanyNameContainingIgnoreCase(name: String): List<LeadJpaEntity>
    
    @Query("SELECT l FROM LeadJpaEntity l WHERE l.status NOT IN ('CONVERTED', 'LOST')")
    fun findActiveLeads(): List<LeadJpaEntity>
    
    // Paginação automática
    @Query("""
        SELECT l FROM LeadJpaEntity l 
        WHERE (:companyName IS NULL OR LOWER(l.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))
        AND (:status IS NULL OR l.status = :status)
    """)
    fun searchLeads(
        @Param("companyName") companyName: String?,
        @Param("status") status: LeadStatus?,
        pageable: Pageable
    ): Page<LeadJpaEntity>
}

// Implementação muito simples
@Repository
class LeadRepositoryImpl(
    private val jpaRepository: LeadJpaRepository
) : LeadRepository {

    override fun save(lead: Lead): Lead {
        val entity = LeadJpaEntity.fromDomain(lead)
        val savedEntity = jpaRepository.save(entity) // ← Automático com validações!
        return savedEntity.toDomain()
    }

    override fun searchLeads(query: SearchLeadsQuery): SearchLeadsResult {
        val pageable = PageRequest.of(query.page, query.size)
        val page = jpaRepository.searchLeads(query.companyNameContains, query.status, pageable)
        
        return SearchLeadsResult(
            leads = page.content.map { it.toDomain() },
            totalElements = page.totalElements, // ← Automático!
            totalPages = page.totalPages, // ← Automático!
            // ... outros campos automáticos
        )
    }
}
```

### **Vantagens**
- ✅ **Muito menos código** (70% redução)
- ✅ **Relacionamentos automáticos** (OneToMany, ManyToOne)
- ✅ **Paginação automática** (Page, Pageable)
- ✅ **Cache de segundo nível**
- ✅ **Validações automáticas**
- ✅ **Transações declarativas**
- ✅ **Lazy loading** automático

### **Desvantagens**
- ❌ **Overhead significativo** (30-50% mais lento)
- ❌ **SQL gerado** (menos controle)
- ❌ **Complexidade em queries avançadas**
- ❌ **"Mágica" do Hibernate** (debugging difícil)
- ❌ **Problemas N+1** se mal configurado
- ❌ **Startup mais lento**

### **Quando usar**
- Projetos com **desenvolvimento rápido** prioritário
- **Relacionamentos complexos** entre entidades
- Equipe com **menos experiência em SQL**
- **Aplicações CRUD padrão** sem otimizações críticas
- **Prototipagem rápida**

---

## 📊 **Comparação Resumida**

| Aspecto | Spring JDBC | Spring Data JDBC | Spring Data JPA |
|---------|-------------|------------------|-----------------|
| **Linhas de código** | ~200 | ~80 | ~50 |
| **Performance** | 100% | 95% | 70% |
| **Controle SQL** | Total | Alto | Limitado |
| **Produtividade** | Baixa | Alta | Muito Alta |
| **Curva aprendizado** | Alta | Média | Baixa |
| **Relacionamentos** | Manual | Limitado | Automático |
| **Cache** | Manual | Manual | Automático |
| **Paginação** | Manual | Manual | Automático |
| **Transações** | Manual | Automático | Automático |
| **Debugging** | Fácil | Médio | Difícil |
| **Startup time** | Rápido | Rápido | Lento |
| **Tamanho dependências** | ~5MB | ~8MB | ~15MB |

---

## 🎯 **Recomendações por Cenário**

### **🏎️ Performance Crítica**
**Use Spring JDBC** quando:
- Performance é **prioridade absoluta**
- Queries **muito complexas** e específicas
- **Otimizações de banco** são necessárias
- Equipe tem **expertise avançada** em SQL

### **⚖️ Equilíbrio (Recomendado)**
**Use Spring Data JDBC** quando:
- Precisa de **boa performance** + **produtividade**
- **Queries moderadamente complexas**
- Quer **métodos automáticos** + **controle SQL**
- **Maioria dos projetos empresariais**

### **🚀 Desenvolvimento Rápido**
**Use Spring Data JPA** quando:
- **Desenvolvimento rápido** é prioridade
- **Relacionamentos complexos** entre entidades
- Equipe tem **pouca experiência** com SQL
- **Prototipagem** ou **aplicações simples**

---

## 🔄 **Migração entre Tecnologias**

### **JDBC → Spring Data JDBC**
```kotlin
// Antes: SQL manual
jdbcTemplate.update(INSERT_SQL, parameters)

// Depois: Método automático
dataJdbcRepository.save(entity)
```

### **Spring Data JDBC → JPA**
```kotlin
// Antes: Entity simples
@Table("leads")
data class LeadEntity(...)

// Depois: Entity JPA
@Entity
@Table(name = "leads")
class LeadJpaEntity(...)
```

---

## 📝 **Conclusão**

Para o **Bridal Cover CRM**, escolhemos **Spring Data JDBC** porque oferece:

✅ **Boa performance** (95% do JDBC manual)  
✅ **Produtividade alta** (60% menos código)  
✅ **SQL nativo** quando necessário  
✅ **Métodos automáticos** para operações básicas  
✅ **Manutenibilidade** adequada para crescimento  

É o **meio termo perfeito** entre controle total e produtividade para um CRM que precisa escalar! 🎯
