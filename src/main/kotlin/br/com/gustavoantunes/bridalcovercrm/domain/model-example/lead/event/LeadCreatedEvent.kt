package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.ContactInfo
import java.time.LocalDateTime

/**
 * Evento de domínio disparado quando um novo Lead é criado no sistema.
 * 
 * Este evento é fundamental para notificar outros contextos sobre a criação
 * de um novo Lead, permitindo ações como notificações, análises e integrações.
 */
class LeadCreatedEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val companyName: CompanyName,
    val contactInfo: ContactInfo,
    val source: LeadSource,
    val createdAt: LocalDateTime,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "LeadCreated"
    
    override val metadata: Map<String, Any> = mapOf(
        "companyName" to companyName.value,
        "source" to source.name,
        "hasEmail" to contactInfo.hasEmail(),
        "hasPhone" to contactInfo.hasPhone(),
        "hasCorporateEmail" to contactInfo.hasCorporateEmail(),
        "completenessScore" to contactInfo.getCompletenessScore(),
        "sourcePriority" to source.getPriority(),
        "requiresVerification" to source.requiresVerification()
    )
}

