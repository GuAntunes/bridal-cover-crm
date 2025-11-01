package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.entity

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.Email
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.Phone
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.SocialMediaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("leads")
data class LeadEntity(
    @Id
    val id: String,
    
    @Column("company_name")
    val companyName: String,
    
    @Column("cnpj")
    val cnpj: String? = null,
    
    @Column("contact_info")
    val contactInfo: String,
    
    @Column("status")
    val status: String,
    
    @Column("source")
    val source: String,
    
    @Column("created_at")
    val createdAt: LocalDateTime,
    
    @Column("updated_at")
    val updatedAt: LocalDateTime
) {
    companion object {
        private val objectMapper = ObjectMapper()
        
        /**
         * Creates a LeadEntity from a Lead domain model
         */
        fun fromDomain(lead: Lead): LeadEntity {
            return LeadEntity(
                id = lead.id.value,
                companyName = lead.name.value,
                cnpj = lead.cnpj?.getDigits(),
                contactInfo = serializeContactInfo(lead.contactInfo),
                status = lead.status.name,
                source = lead.source.name,
                createdAt = lead.createdAt,
                updatedAt = lead.updatedAt
            )
        }
        
        /**
         * Serializes ContactInfo to JSON string
         */
        private fun serializeContactInfo(contactInfo: ContactInfo): String {
            val data = mutableMapOf<String, Any>()
            
            contactInfo.email?.let { data["email"] = it.value }
            contactInfo.phone?.let { 
                data["phone"] = mapOf(
                    "value" to it.value,
                    "countryCode" to it.countryCode,
                    "areaCode" to it.areaCode
                )
            }
            contactInfo.website?.let { data["website"] = it }
            if (contactInfo.socialMedia.isNotEmpty()) {
                data["socialMedia"] = contactInfo.socialMedia.mapKeys { it.key.name }
            }
            
            return objectMapper.writeValueAsString(data)
        }
    }
    
    /**
     * Converts this LeadEntity to a Lead domain model
     */
    fun toDomain(): Lead {
        return Lead(
            id = LeadId.fromString(id),
            name = CompanyName(companyName),
            cnpj = cnpj?.let { CNPJ.fromString(it) },
            contactInfo = deserializeContactInfo(contactInfo),
            status = LeadStatus.valueOf(status),
            source = LeadSource.valueOf(source),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    /**
     * Deserializes ContactInfo from JSON string
     */
    private fun deserializeContactInfo(json: String): ContactInfo {
        val data: Map<String, Any?> = objectMapper.readValue(json)
        
        val email = (data["email"] as? String)?.let { Email(it) }
        
        val phone = (data["phone"] as? Map<*, *>)?.let { phoneData ->
            Phone(
                value = phoneData["value"] as String,
                countryCode = phoneData["countryCode"] as? String ?: "55",
                areaCode = phoneData["areaCode"] as? String
            )
        }
        
        val website = data["website"] as? String
        
        val socialMedia = (data["socialMedia"] as? Map<*, *>)?.mapKeys { 
            SocialMediaType.valueOf(it.key as String) 
        }?.mapValues { 
            it.value as String 
        } ?: emptyMap()
        
        return ContactInfo(
            email = email,
            phone = phone,
            website = website,
            socialMedia = socialMedia
        )
    }
}


