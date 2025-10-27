package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus

/**
 * Evento de domínio disparado quando o status de um Lead é alterado.
 * 
 * Este evento é crucial para acompanhar a progressão do Lead no funil de vendas
 * e disparar ações automáticas baseadas no novo status.
 */
class LeadStatusChangedEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val previousStatus: LeadStatus,
    val newStatus: LeadStatus,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "LeadStatusChanged"
    
    override val metadata: Map<String, Any> = mapOf(
        "previousStatus" to previousStatus.name,
        "newStatus" to newStatus.name,
        "isProgression" to isProgression(),
        "isTerminal" to newStatus.isTerminal(),
        "wasActivated" to (previousStatus.isTerminal() && newStatus.isActive()),
        "wasDeactivated" to (previousStatus.isActive() && newStatus.isTerminal())
    )
    
    /**
     * Verifica se a mudança representa uma progressão no funil
     */
    private fun isProgression(): Boolean {
        val statusOrder = listOf(
            LeadStatus.NEW,
            LeadStatus.CONTACTED,
            LeadStatus.QUALIFIED,
            LeadStatus.PROPOSAL_SENT,
            LeadStatus.NEGOTIATING,
            LeadStatus.CONVERTED
        )
        
        val previousIndex = statusOrder.indexOf(previousStatus)
        val newIndex = statusOrder.indexOf(newStatus)
        
        return previousIndex != -1 && newIndex != -1 && newIndex > previousIndex
    }
}

