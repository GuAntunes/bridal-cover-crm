package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.repository

import br.com.gustavoantunes.bridalcovercrm.domain.port.out.repository.LeadRepository
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.entity.LeadEntity
import org.springframework.stereotype.Component

/**
 * Adapter that implements the LeadRepository port.
 * 
 * This class acts as a bridge between the domain layer (using Lead aggregate)
 * and the infrastructure layer (using Spring Data JDBC and LeadEntity).
 * 
 * It follows the Hexagonal Architecture pattern, where:
 * - LeadRepository (port/out) is the interface defined by the domain
 * - LeadRepositoryAdapter (adapter) is the implementation in the infrastructure
 * - LeadDataJdbcRepository is the Spring Data repository
 * - LeadEntity handles the conversion between domain and persistence models via its own methods
 */
@Component
class LeadRepositoryAdapter(
    private val dataRepository: LeadDataJdbcRepository
) : LeadRepository {

    /**
     * Saves a Lead aggregate to the database.
     * 
     * Converts the domain model to entity, persists it,
     * and converts back to domain model.
     */
    override fun save(lead: Lead): Lead {
        val entity = LeadEntity.fromDomain(lead)
        val savedEntity = dataRepository.save(entity)
        return savedEntity.toDomain()
    }

    /**
     * Finds a Lead by its ID.
     * 
     * Returns null if no Lead is found with the given ID.
     */
    override fun findById(id: LeadId): Lead? {
        val uuid = java.util.UUID.fromString(id.value)
        return dataRepository.findById(uuid)
            .map { it.toDomain() }
            .orElse(null)
    }

    /**
     * Finds all Leads with pagination support.
     * 
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return List of Leads for the requested page
     */
    override fun findAll(page: Int, size: Int): List<Lead> {
        val pageable = org.springframework.data.domain.PageRequest.of(page, size)
        return dataRepository.findAllBy(pageable)
            .map { it.toDomain() }
    }

    /**
     * Counts the total number of Leads in the database.
     * 
     * @return Total number of Leads
     */
    override fun count(): Long {
        return dataRepository.count()
    }

    /**
     * Deletes a Lead by its ID.
     * 
     * @param id The LeadId to delete
     */
    override fun deleteById(id: LeadId) {
        val uuid = java.util.UUID.fromString(id.value)
        dataRepository.deleteById(uuid)
    }
}


