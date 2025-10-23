package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.ContactInfo

/**
 * Evento de domínio disparado quando as informações de contato de um Lead são atualizadas.
 * 
 * Este evento permite que outros contextos reajam às mudanças nas informações de contato,
 * como sincronização com sistemas externos ou validação de dados.
 */
class LeadContactInfoUpdatedEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val previousContactInfo: ContactInfo,
    val newContactInfo: ContactInfo,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "LeadContactInfoUpdated"
    
    override val metadata: Map<String, Any> = mapOf(
        "emailChanged" to (previousContactInfo.email != newContactInfo.email),
        "phoneChanged" to (previousContactInfo.phone != newContactInfo.phone),
        "websiteChanged" to (previousContactInfo.website != newContactInfo.website),
        "socialMediaChanged" to (previousContactInfo.socialMedia != newContactInfo.socialMedia),
        "previousCompletenessScore" to previousContactInfo.getCompletenessScore(),
        "newCompletenessScore" to newContactInfo.getCompletenessScore(),
        "completenessImproved" to (newContactInfo.getCompletenessScore() > previousContactInfo.getCompletenessScore()),
        "becameComplete" to (!previousContactInfo.isComplete() && newContactInfo.isComplete()),
        "becameIncomplete" to (previousContactInfo.isComplete() && !newContactInfo.isComplete()),
        "addedCorporateEmail" to (!previousContactInfo.hasCorporateEmail() && newContactInfo.hasCorporateEmail()),
        "addedMobilePhone" to (!previousContactInfo.hasMobilePhone() && newContactInfo.hasMobilePhone())
    )
}

