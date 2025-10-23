package br.com.gustavoantunes.bridalcovercrm.application.port.out

import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.shared.Email

/**
 * Repository interface for the Lead aggregate.
 *
 * Defines the persistence operations necessary to manage Leads,
 * following DDD principles and keeping the domain independent
 * from the persistence infrastructure.
 */
interface LeadRepository {

    /**
     * Saves a Lead in the repository
     */
    fun save(lead: Lead): Lead

    /**
     * Finds a Lead by its ID
     */
    fun findById(id: LeadId): Lead?

    /**
     * Finds a Lead by CNPJ
     */
    fun findByCNPJ(cnpj: CNPJ): Lead?

    /**
     * Finds a Lead by email
     */
    fun findByEmail(email: Email): Lead?
}