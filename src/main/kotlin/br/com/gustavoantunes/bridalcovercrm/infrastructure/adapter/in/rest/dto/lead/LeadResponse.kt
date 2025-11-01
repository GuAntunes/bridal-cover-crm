package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Response DTO for Lead operations.
 * 
 * This is part of the infrastructure layer and converts domain entities
 * into HTTP JSON responses.
 */
data class LeadResponse(
    @JsonProperty("id")
    val id: String,
    
    @JsonProperty("companyName")
    val companyName: String,
    
    @JsonProperty("cnpj")
    val cnpj: String? = null,
    
    @JsonProperty("contactInfo")
    val contactInfo: ContactInfoResponse,
    
    @JsonProperty("status")
    val status: String,
    
    @JsonProperty("source")
    val source: String,
    
    @JsonProperty("createdAt")
    val createdAt: LocalDateTime,
    
    @JsonProperty("updatedAt")
    val updatedAt: LocalDateTime
) {

    companion object {
        /**
         * Converts a domain Lead entity into a response DTO.
         */
        fun fromDomain(lead: Lead): LeadResponse {
            return LeadResponse(
                id = lead.id.toString(),
                companyName = lead.name.toString(),
                cnpj = lead.cnpj?.toString(),
                contactInfo = ContactInfoResponse.fromDomain(lead.contactInfo),
                status = lead.status.name,
                source = lead.source.name,
                createdAt = lead.createdAt,
                updatedAt = lead.updatedAt
            )
        }
    }
}

/**
 * Response DTO for Contact Information.
 */
data class ContactInfoResponse(
    @JsonProperty("email")
    val email: String? = null,
    
    @JsonProperty("phone")
    val phone: String? = null,
    
    @JsonProperty("phoneFormatted")
    val phoneFormatted: String? = null,
    
    @JsonProperty("website")
    val website: String? = null,
    
    @JsonProperty("socialMedia")
    val socialMedia: Map<String, String>? = null,
    
    @JsonProperty("hasEmail")
    val hasEmail: Boolean,
    
    @JsonProperty("hasPhone")
    val hasPhone: Boolean,
    
    @JsonProperty("isComplete")
    val isComplete: Boolean
) {

    companion object {
        /**
         * Converts domain ContactInfo into a response DTO.
         */
        fun fromDomain(contactInfo: br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo): ContactInfoResponse {
            return ContactInfoResponse(
                email = contactInfo.email?.toString(),
                phone = contactInfo.phone?.getFullNumber(),
                phoneFormatted = contactInfo.phone?.format(),
                website = contactInfo.website,
                socialMedia = contactInfo.socialMedia.mapKeys { it.key.name },
                hasEmail = contactInfo.hasEmail(),
                hasPhone = contactInfo.hasPhone(),
                isComplete = contactInfo.isComplete()
            )
        }
    }
}

