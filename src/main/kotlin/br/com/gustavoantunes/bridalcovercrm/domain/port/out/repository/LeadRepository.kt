package br.com.gustavoantunes.bridalcovercrm.domain.port.out.repository

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId

interface LeadRepository {
    fun save(lead: Lead): Lead
    fun findById(id: LeadId): Lead?
}


