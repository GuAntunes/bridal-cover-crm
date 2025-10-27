package br.com.gustavoantunes.bridalcovercrm.application.service.lead

import br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead.DeleteLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.application.port.out.repository.LeadRepository
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import org.springframework.stereotype.Service

@Service
class DeleteLeadService(
    private val leadRepository: LeadRepository
) : DeleteLeadUseCase {

    override fun deleteLead(leadId: LeadId) {
        val existingLead = leadRepository.findById(leadId)
            ?: throw IllegalArgumentException("Lead not found with id: $leadId")
        
        leadRepository.deleteById(leadId)
    }
}
