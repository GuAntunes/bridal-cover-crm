package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.contactAttempt

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.LeadId

/**
 * Evento de domínio disparado quando uma nova tentativa de contato é adicionada a um Lead.
 * 
 * Este evento é importante para acompanhar a atividade de vendas e disparar
 * ações como notificações, análises de performance e agendamentos automáticos.
 */
class ContactAttemptAddedEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val contactAttempt: ContactAttempt,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "ContactAttemptAdded"
    
    override val metadata: Map<String, Any> = mapOf(
        "channel" to contactAttempt.channel.name,
        "result" to contactAttempt.result.name,
        "wasSuccessful" to contactAttempt.wasSuccessful(),
        "requiresFollowUp" to contactAttempt.requiresFollowUp(),
        "hasNotes" to (contactAttempt.notes != null),
        "hasDuration" to (contactAttempt.duration != null),
        "wasLongAttempt" to contactAttempt.wasLongAttempt(),
        "isRecent" to contactAttempt.isRecent(),
        "qualificationScore" to contactAttempt.result.getQualificationScore(),
        "channelEffectiveness" to contactAttempt.channel.getEffectivenessRating(),
        "isRealTimeChannel" to contactAttempt.channel.isRealTime(),
        "isDigitalChannel" to contactAttempt.channel.isDigital(),
        "nextFollowUpDate" to (contactAttempt.nextFollowUp?.toString() ?: "")
    )
}
