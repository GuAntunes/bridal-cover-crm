package br.com.gustavoantunes.bridalcovercrm.domain.port.out.repository

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId

interface LeadRepository {
    fun save(lead: Lead): Lead
    fun findById(id: LeadId): Lead?
    fun findAll(page: Int, size: Int): List<Lead>
    fun count(): Long
    fun deleteById(id: LeadId)
}


