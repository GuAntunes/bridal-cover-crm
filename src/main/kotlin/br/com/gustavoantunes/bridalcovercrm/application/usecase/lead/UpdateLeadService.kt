package br.com.gustavoantunes.bridalcovercrm.application.usecase.lead

import br.com.gustavoantunes.bridalcovercrm.application.dto.lead.UpdateLeadCommand
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadNotFoundException
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.*
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.UpdateLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.out.repository.LeadRepository
import org.springframework.stereotype.Service

@Service
class UpdateLeadService(
    private val leadRepository: LeadRepository
) : UpdateLeadUseCase {

    override fun execute(command: UpdateLeadCommand): Lead {
        val leadId = LeadId.fromString(command.id)
        val existingLead = leadRepository.findById(leadId)
            ?: throw LeadNotFoundException("Lead with ID ${command.id} not found")

        // Update name if provided
        command.companyName?.let {
            existingLead.name = CompanyName(it)
        }

        // Update CNPJ if provided
        command.cnpj?.let {
            existingLead.cnpj = CNPJ.fromString(it)
        }

        // Update contact info if any field is provided
        if (command.email != null || command.phone != null || command.instagram != null) {
            val newContactInfo = ContactInfo(
                email = command.email?.let { Email(it) } ?: existingLead.contactInfo.email,
                phone = command.phone?.let { Phone.fromBrazilianNumber(it) } ?: existingLead.contactInfo.phone,
                socialMedia = command.instagram?.let {
                    mapOf(SocialMediaType.INSTAGRAM to it)
                } ?: existingLead.contactInfo.socialMedia
            )
            existingLead.contactInfo = newContactInfo
        }

        // Update status if provided
        command.status?.let { newStatus ->
            existingLead.status = newStatus
        }

        existingLead.updatedAt = java.time.LocalDateTime.now()

        return leadRepository.save(existingLead)
    }
}

