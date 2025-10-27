package br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId

interface DeleteLeadUseCase {
    fun deleteLead(leadId: LeadId)
}