package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CompanyName

/**
 * Evento de domínio disparado quando o nome da empresa de um Lead é atualizado.
 * 
 * Este evento permite rastrear mudanças no nome da empresa e sincronizar
 * com sistemas externos ou análises de dados.
 */
class LeadCompanyNameUpdatedEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val previousName: CompanyName,
    val newName: CompanyName,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "LeadCompanyNameUpdated"
    
    override val metadata: Map<String, Any> = mapOf(
        "previousName" to previousName.value,
        "newName" to newName.value,
        "previousLength" to previousName.length(),
        "newLength" to newName.length(),
        "wasLong" to previousName.isLong(),
        "isLong" to newName.isLong(),
        "lengthChanged" to (previousName.length() != newName.length()),
        "significantChange" to isSignificantChange()
    )
    
    /**
     * Verifica se a mudança é significativa (mais que correção ortográfica)
     */
    private fun isSignificantChange(): Boolean {
        val previousWords = previousName.value.lowercase().split("\\s+".toRegex()).toSet()
        val newWords = newName.value.lowercase().split("\\s+".toRegex()).toSet()
        
        val commonWords = previousWords.intersect(newWords)
        val totalWords = previousWords.union(newWords)
        
        // Se menos de 70% das palavras são comuns, considera mudança significativa
        return commonWords.size.toDouble() / totalWords.size < 0.7
    }
}

