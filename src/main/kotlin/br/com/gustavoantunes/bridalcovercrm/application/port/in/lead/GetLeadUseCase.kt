package br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId

interface GetLeadUseCase {
    fun getLeadById(leadId: LeadId): Lead?
}
