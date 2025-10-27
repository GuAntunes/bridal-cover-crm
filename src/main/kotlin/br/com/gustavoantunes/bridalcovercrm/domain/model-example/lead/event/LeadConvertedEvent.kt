package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.event

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.DomainEvent
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.ContactInfo

/**
 * Evento de domínio disparado quando um Lead é convertido em Cliente.
 * 
 * Este é um dos eventos mais importantes do sistema, pois representa
 * o sucesso do processo de vendas e dispara a criação do cliente
 * e outras ações relacionadas.
 */
class LeadConvertedEvent(
    override val aggregateId: String,
    val leadId: LeadId,
    val companyName: CompanyName,
    val cnpj: CNPJ?,
    val contactInfo: ContactInfo,
    override val userId: String? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : DomainEvent() {
    
    override val eventType: String = "LeadConverted"
    
    override val metadata: Map<String, Any> = mapOf(
        "companyName" to companyName.value,
        "hasCNPJ" to (cnpj != null),
        "cnpj" to (cnpj?.format() ?: ""),
        "isHeadOffice" to (cnpj?.isHeadOffice() ?: false),
        "hasEmail" to contactInfo.hasEmail(),
        "hasPhone" to contactInfo.hasPhone(),
        "hasCorporateEmail" to contactInfo.hasCorporateEmail(),
        "hasMobilePhone" to contactInfo.hasMobilePhone(),
        "completenessScore" to contactInfo.getCompletenessScore(),
        "isComplete" to contactInfo.isComplete(),
        "primaryContact" to (contactInfo.getPrimaryContact() ?: "")
    )
}

