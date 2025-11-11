package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead

import br.com.gustavoantunes.bridalcovercrm.application.dto.lead.RegisterLeadCommand
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.*
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request DTO for registering a new lead.
 * 
 * This is part of the infrastructure layer and converts HTTP JSON
 * into domain commands.
 */
data class LeadRequest(
    @JsonProperty("companyName")
    @field:NotBlank(message = "Company name is required")
    @field:Size(min = 2, max = 200, message = "Company name must be between 2 and 200 characters")
    val companyName: String,
    
    @JsonProperty("cnpj")
    val cnpj: String? = null,
    
    @JsonProperty("email")
    val email: String? = null,
    
    @JsonProperty("phone")
    val phone: String? = null,
    
    @JsonProperty("website")
    val website: String? = null,
    
    @JsonProperty("socialMedia")
    val socialMedia: Map<String, String>? = null,
    
    @JsonProperty("source")
    @field:NotBlank(message = "Source is required")
    val source: String
) {

    /**
     * Converts the HTTP request into a domain command.
     * 
     * This method performs validation and transformation of primitive types
     * into domain value objects.
     */
    fun toCommand(): RegisterLeadCommand {
        val companyNameVO = CompanyName(companyName)

        val cnpjVO = cnpj?.takeIf { it.isNotBlank() }?.let { CNPJ.fromString(it) }
        
        val emailVO = email?.takeIf { it.isNotBlank() }?.let { Email(it) }
        val phoneVO = phone?.takeIf { it.isNotBlank() }?.let { Phone.fromBrazilianNumber(it) }
        
        val socialMediaMap = socialMedia
            ?.filterValues { it.isNotBlank() }
            ?.mapKeys { (key, _) -> 
                SocialMediaType.valueOf(key.uppercase())
            } ?: emptyMap()
        
        val contactInfo = ContactInfo(
            email = emailVO,
            phone = phoneVO,
            website = website?.takeIf { it.isNotBlank() },
            socialMedia = socialMediaMap
        )
        
        val leadSource = try {
            LeadSource.valueOf(source.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid lead source: $source. Valid values are: ${LeadSource.entries.joinToString()}")
        }
        
        return RegisterLeadCommand(
            companyName = companyNameVO,
            cnpj = cnpjVO,
            contactInfo = contactInfo,
            source = leadSource
        )
    }
}

