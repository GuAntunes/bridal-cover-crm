package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.AggregateRootWithId
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.contactAttempt.ContactAttempt
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.contactAttempt.ContactAttemptAddedEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event.LeadCNPJUpdatedEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event.LeadCompanyNameUpdatedEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event.LeadContactInfoUpdatedEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event.LeadConvertedEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event.LeadCreatedEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event.LeadLostEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event.LeadStatusChangedEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.ContactInfo
import java.time.LocalDateTime

/**
 * Lead - represents a potential client company in the sales process.
 *
 * The Lead is the central point of the prospecting and qualification process,
 * holding all the information necessary for follow-up
 * and eventual conversion into a client.
 */
class Lead(
    override val id: LeadId,
    var name: CompanyName,
    var cnpj: CNPJ? = null,
    var contactInfo: ContactInfo,
    var status: LeadStatus = LeadStatus.NEW,
    val source: LeadSource,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) : AggregateRootWithId<LeadId>() {
    
    private val _contactAttempts = mutableListOf<ContactAttempt>()
    val contactAttempts: List<ContactAttempt> get() = _contactAttempts.toList()
    
    init {
        require(name.value.isNotBlank()) { "Nome da empresa não pode estar vazio" }
        
        // Gera evento de criação do Lead
        addDomainEvent(
            LeadCreatedEvent(
                aggregateId = id.value,
                leadId = id,
                companyName = name,
                contactInfo = contactInfo,
                source = source,
                createdAt = createdAt
            )
        )
    }
    
    /**
     * Atualiza o status do Lead
     */
    fun updateStatus(newStatus: LeadStatus, userId: String? = null) {
        require(status.canTransitionTo(newStatus)) { 
            "Não é possível transicionar de ${status.name} para ${newStatus.name}" 
        }
        
        val previousStatus = this.status
        this.status = newStatus
        this.updatedAt = LocalDateTime.now()
        
        addDomainEvent(
            LeadStatusChangedEvent(
                aggregateId = id.value,
                leadId = id,
                previousStatus = previousStatus,
                newStatus = newStatus,
                userId = userId
            )
        )
    }
    
    /**
     * Atualiza as informações de contato
     */
    fun updateContactInfo(newContactInfo: ContactInfo, userId: String? = null) {
        val previousContactInfo = this.contactInfo
        this.contactInfo = newContactInfo
        this.updatedAt = LocalDateTime.now()
        
        addDomainEvent(
            LeadContactInfoUpdatedEvent(
                aggregateId = id.value,
                leadId = id,
                previousContactInfo = previousContactInfo,
                newContactInfo = newContactInfo,
                userId = userId
            )
        )
    }
    
    /**
     * Adiciona uma tentativa de contato
     */
    fun addContactAttempt(attempt: ContactAttempt, userId: String? = null) {
        require(attempt.leadId == this.id) { "Tentativa de contato deve pertencer a este Lead" }
        
        _contactAttempts.add(attempt)
        this.updatedAt = LocalDateTime.now()
        
        // Atualiza status automaticamente se necessário
        if (status == LeadStatus.NEW && attempt.wasSuccessful()) {
            updateStatus(LeadStatus.CONTACTED, userId)
        }
        
        addDomainEvent(
            ContactAttemptAddedEvent(
                aggregateId = id.value,
                leadId = id,
                contactAttempt = attempt,
                userId = userId
            )
        )
    }
    
    /**
     * Retorna a última tentativa de contato
     */
    fun getLastContactAttempt(): ContactAttempt? {
        return _contactAttempts.maxByOrNull { it.attemptDate }
    }
    
    /**
     * Retorna tentativas de contato que requerem follow-up
     */
    fun getPendingFollowUps(): List<ContactAttempt> {
        return _contactAttempts.filter { it.requiresFollowUp() }
    }
    
    /**
     * Retorna tentativas de contato vencidas
     */
    fun getOverdueFollowUps(): List<ContactAttempt> {
        return _contactAttempts.filter { it.isFollowUpOverdue() }
    }
    
    /**
     * Verifica se o Lead está qualificado
     */
    fun isQualified(): Boolean {
        return status == LeadStatus.QUALIFIED || 
               status == LeadStatus.PROPOSAL_SENT || 
               status == LeadStatus.NEGOTIATING
    }
    
    /**
     * Verifica se o Lead pode ser convertido em cliente
     */
    fun canBeConverted(): Boolean {
        return status.canTransitionTo(LeadStatus.CONVERTED) && 
               hasSuccessfulContacts() && 
               contactInfo.isComplete()
    }
    
    /**
     * Converte o Lead para status CONVERTED
     */
    fun convertToClient(userId: String? = null) {
        require(canBeConverted()) { "Lead não pode ser convertido no status atual" }
        
        updateStatus(LeadStatus.CONVERTED, userId)
        
        addDomainEvent(
            LeadConvertedEvent(
                aggregateId = id.value,
                leadId = id,
                companyName = name,
                cnpj = cnpj,
                contactInfo = contactInfo,
                userId = userId
            )
        )
    }
    
    /**
     * Marca o Lead como perdido
     */
    fun markAsLost(reason: String? = null, userId: String? = null) {
        require(status.canTransitionTo(LeadStatus.LOST)) { 
            "Lead não pode ser marcado como perdido no status atual" 
        }
        
        updateStatus(LeadStatus.LOST, userId)
        
        addDomainEvent(
            LeadLostEvent(
                aggregateId = id.value,
                leadId = id,
                reason = reason,
                userId = userId
            )
        )
    }
    
    /**
     * Verifica se tem contatos bem-sucedidos
     */
    private fun hasSuccessfulContacts(): Boolean {
        return _contactAttempts.any { it.wasSuccessful() }
    }
    
    /**
     * Calcula a pontuação de qualificação do Lead
     */
    fun calculateQualificationScore(): Int {
        var score = 0
        
        // Pontuação base por fonte
        score += source.getPriority() * 10
        
        // Pontuação por completude das informações
        score += contactInfo.getCompletenessScore() / 2
        
        // Pontuação por tentativas de contato bem-sucedidas
        val successfulAttempts = _contactAttempts.filter { it.wasSuccessful() }
        score += successfulAttempts.size * 15
        
        // Pontuação por resultados específicos
        _contactAttempts.forEach { attempt ->
            score += attempt.result.getQualificationScore()
        }
        
        // Bônus por CNPJ (empresa formalizada)
        if (cnpj != null) score += 20
        
        // Bônus por email corporativo
        if (contactInfo.hasCorporateEmail()) score += 15
        
        return minOf(score, 100)
    }
    
    /**
     * Verifica se o Lead está ativo (não convertido nem perdido)
     */
    fun isActive(): Boolean {
        return status.isActive()
    }
    
    /**
     * Retorna um resumo do Lead para relatórios
     */
    fun getSummary(): String {
        return buildString {
            append("${name.value} - ${status.name}")
            append(" | Fonte: ${source.getDisplayName()}")
            append(" | Tentativas: ${_contactAttempts.size}")
            getLastContactAttempt()?.let { 
                append(" | Último contato: ${it.attemptDate.toLocalDate()}")
            }
        }
    }
    
    /**
     * Atualiza o CNPJ do Lead
     */
    fun updateCNPJ(newCNPJ: CNPJ?, userId: String? = null) {
        val previousCNPJ = this.cnpj
        this.cnpj = newCNPJ
        this.updatedAt = LocalDateTime.now()
        
        addDomainEvent(
            LeadCNPJUpdatedEvent(
                aggregateId = id.value,
                leadId = id,
                previousCNPJ = previousCNPJ,
                newCNPJ = newCNPJ,
                userId = userId
            )
        )
    }
    
    /**
     * Atualiza o nome da empresa
     */
    fun updateCompanyName(newName: CompanyName, userId: String? = null) {
        val previousName = this.name
        this.name = newName
        this.updatedAt = LocalDateTime.now()
        
        addDomainEvent(
            LeadCompanyNameUpdatedEvent(
                aggregateId = id.value,
                leadId = id,
                previousName = previousName,
                newName = newName,
                userId = userId
            )
        )
    }
}
