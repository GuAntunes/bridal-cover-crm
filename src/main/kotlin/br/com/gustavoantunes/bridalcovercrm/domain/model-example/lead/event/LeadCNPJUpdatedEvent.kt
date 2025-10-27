package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CNPJ

/**
 * Evento de domínio disparado quando o CNPJ de um Lead é atualizado.
 * 
 * Este evento é importante para validações, sincronizações com sistemas
 * externos e análises de formalização de empresas.
 */
class LeadCNPJUpdatedEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val previousCNPJ: CNPJ?,
    val newCNPJ: CNPJ?,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "LeadCNPJUpdated"
    
    override val metadata: Map<String, Any> = mapOf(
        "hadCNPJ" to (previousCNPJ != null),
        "hasCNPJ" to (newCNPJ != null),
        "previousCNPJ" to (previousCNPJ?.format() ?: ""),
        "newCNPJ" to (newCNPJ?.format() ?: ""),
        "wasAdded" to (previousCNPJ == null && newCNPJ != null),
        "wasRemoved" to (previousCNPJ != null && newCNPJ == null),
        "wasChanged" to (previousCNPJ != null && newCNPJ != null && previousCNPJ != newCNPJ),
        "isHeadOffice" to (newCNPJ?.isHeadOffice() ?: false),
        "baseNumberChanged" to (previousCNPJ?.getBaseNumber() != newCNPJ?.getBaseNumber())
    )
}

