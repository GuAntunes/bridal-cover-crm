package br.com.gustavoantunes.bridalcovercrm.infrastructure.persistence.adapter

import br.com.gustavoantunes.bridalcovercrm.application.port.out.repository.LeadRepository
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.infrastructure.persistence.mapper.LeadMapper
import br.com.gustavoantunes.bridalcovercrm.infrastructure.persistence.repository.LeadDataJdbcRepository
import org.springframework.stereotype.Component

/**
 * Adapter that implements the LeadRepository port.
 * 
 * This class acts as a bridge between the domain layer (using Lead aggregate)
 * and the infrastructure layer (using Spring Data JDBC and LeadEntity).
 * 
 * It follows the Hexagonal Architecture pattern, where:
 * - LeadRepository (port/out) is the interface defined by the application
 * - LeadRepositoryAdapter (adapter) is the implementation in the infrastructure
 * - LeadDataJdbcRepository is the Spring Data repository
 * - LeadMapper handles the conversion between domain and persistence models
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
        val entity = LeadMapper.toEntity(lead)
        val savedEntity = dataRepository.save(entity)
        return LeadMapper.toDomain(savedEntity)
    }

    /**
     * Finds a Lead by its ID.
     * 
     * Returns null if no Lead is found with the given ID.
     */
    override fun findById(id: LeadId): Lead? {
        return dataRepository.findById(id.value)
            .map { LeadMapper.toDomain(it) }
            .orElse(null)
    }
}

