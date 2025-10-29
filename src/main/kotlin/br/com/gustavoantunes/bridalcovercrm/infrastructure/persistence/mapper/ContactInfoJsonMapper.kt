package br.com.gustavoantunes.bridalcovercrm.infrastructure.persistence.mapper

import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.Email
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.Phone
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.SocialMediaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Mapper responsible for converting ContactInfo to/from JSON.
 * Used for storing contact information in the database as JSONB.
 */
object ContactInfoJsonMapper {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * Data class used for JSON serialization/deserialization
     */
    private data class ContactInfoJson(
        val email: String? = null,
        val phone: PhoneJson? = null,
        val website: String? = null,
        val socialMedia: Map<String, String> = emptyMap()
    )

    private data class PhoneJson(
        val value: String,
        val countryCode: String = "55",
        val areaCode: String? = null
    )

    /**
     * Converts ContactInfo domain object to JSON string
     */
    fun toJson(contactInfo: ContactInfo): String {
        val json = ContactInfoJson(
            email = contactInfo.email?.value,
            phone = contactInfo.phone?.let { phone ->
                PhoneJson(
                    value = phone.value,
                    countryCode = phone.countryCode,
                    areaCode = phone.areaCode
                )
            },
            website = contactInfo.website,
            socialMedia = contactInfo.socialMedia.mapKeys { it.key.name }
        )
        
        return objectMapper.writeValueAsString(json)
    }

    /**
     * Converts JSON string to ContactInfo domain object
     */
    fun fromJson(json: String): ContactInfo {
        val contactInfoJson = objectMapper.readValue<ContactInfoJson>(json)
        
        return ContactInfo(
            email = contactInfoJson.email?.let { Email(it) },
            phone = contactInfoJson.phone?.let { phoneJson ->
                Phone(
                    value = phoneJson.value,
                    countryCode = phoneJson.countryCode,
                    areaCode = phoneJson.areaCode
                )
            },
            website = contactInfoJson.website,
            socialMedia = contactInfoJson.socialMedia.mapKeys { 
                SocialMediaType.valueOf(it.key) 
            }
        )
    }
}

