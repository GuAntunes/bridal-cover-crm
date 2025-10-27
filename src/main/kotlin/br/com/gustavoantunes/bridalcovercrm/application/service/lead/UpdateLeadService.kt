package br.com.gustavoantunes.bridalcovercrm.application.service.lead

import br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead.UpdateLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.application.port.out.repository.LeadRepository
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UpdateLeadService(
    private val leadRepository: LeadRepository
) : UpdateLeadUseCase {

    override fun updateLead(command: UpdateLeadUseCase.UpdateLeadCommand): Lead {
        val existingLead = leadRepository.findById(command.leadId)
            ?: throw IllegalArgumentException("Lead not found with id: ${command.leadId}")

        command.companyName?.let { existingLead.name = it }
        command.cnpj?.let { existingLead.cnpj = it }
        command.contactInfo?.let { existingLead.contactInfo = it }
        
        existingLead.updatedAt = LocalDateTime.now()
        
        return leadRepository.save(existingLead)
    }

    override fun updateLeadStatus(command: UpdateLeadUseCase.UpdateLeadStatusCommand): Lead {
        val existingLead = leadRepository.findById(command.leadId)
            ?: throw IllegalArgumentException("Lead not found with id: ${command.leadId}")

        if (!existingLead.status.canTransitionTo(command.newStatus)) {
            throw IllegalStateException("Cannot transition from ${existingLead.status} to ${command.newStatus}")
        }

        existingLead.status = command.newStatus
        existingLead.updatedAt = LocalDateTime.now()
        
        return leadRepository.save(existingLead)
    }
}
