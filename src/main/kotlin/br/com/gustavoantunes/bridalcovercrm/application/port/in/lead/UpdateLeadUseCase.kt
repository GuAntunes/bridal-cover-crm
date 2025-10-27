package br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo

interface UpdateLeadUseCase {
    fun updateLead(command: UpdateLeadCommand): Lead
    fun updateLeadStatus(command: UpdateLeadStatusCommand): Lead
    
    data class UpdateLeadCommand(
        val leadId: LeadId,
        val companyName: CompanyName? = null,
        val cnpj: CNPJ? = null,
        val contactInfo: ContactInfo? = null
    )
    
    data class UpdateLeadStatusCommand(
        val leadId: LeadId,
        val newStatus: LeadStatus
    )
}