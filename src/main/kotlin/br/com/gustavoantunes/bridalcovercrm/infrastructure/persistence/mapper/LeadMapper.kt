package br.com.gustavoantunes.bridalcovercrm.infrastructure.persistence.mapper

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.infrastructure.persistence.entity.LeadEntity

/**
 * Mapper responsible for converting between Lead domain model and LeadEntity.
 * 
 * Follows the principle of keeping the domain model independent from
 * infrastructure concerns (database, persistence framework, etc).
 */
object LeadMapper {

    /**
     * Converts Lead domain model to LeadEntity for persistence
     */
    fun toEntity(lead: Lead): LeadEntity {
        return LeadEntity(
            id = lead.id.value,
            companyName = lead.name.value,
            cnpj = lead.cnpj?.getDigits(),
            contactInfo = ContactInfoJsonMapper.toJson(lead.contactInfo),
            status = lead.status.name,
            source = lead.source.name,
            createdAt = lead.createdAt,
            updatedAt = lead.updatedAt
        )
    }

    /**
     * Converts LeadEntity from database to Lead domain model
     */
    fun toDomain(entity: LeadEntity): Lead {
        return Lead(
            id = LeadId.fromString(entity.id),
            name = CompanyName(entity.companyName),
            cnpj = entity.cnpj?.let { CNPJ.fromString(it) },
            contactInfo = ContactInfoJsonMapper.fromJson(entity.contactInfo),
            status = LeadStatus.valueOf(entity.status),
            source = LeadSource.valueOf(entity.source),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * Converts a list of LeadEntity to a list of Lead domain models
     */
    fun toDomainList(entities: List<LeadEntity>): List<Lead> {
        return entities.map { toDomain(it) }
    }

    /**
     * Converts a list of Lead domain models to a list of LeadEntity
     */
    fun toEntityList(leads: List<Lead>): List<LeadEntity> {
        return leads.map { toEntity(it) }
    }
}

