package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.LeadId

/**
 * Evento de domínio disparado quando um Lead é marcado como perdido.
 * 
 * Este evento é importante para análises de performance, identificação
 * de padrões de perda e melhoria do processo de vendas.
 */
class LeadLostEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val reason: String? = null,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "LeadLost"
    
    override val metadata: Map<String, Any> = mapOf(
        "hasReason" to (reason != null),
        "reason" to (reason ?: ""),
        "reasonLength" to (reason?.length ?: 0)
    )
}

