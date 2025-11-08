package br.com.gustavoantunes.bridalcovercrm.application.usecase.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadNotFoundException
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.DeleteLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.out.repository.LeadRepository
import org.springframework.stereotype.Service

@Service
class DeleteLeadService(
    private val leadRepository: LeadRepository
) : DeleteLeadUseCase {

    override fun execute(id: String) {
        val leadId = LeadId.fromString(id)
        // Verify if lead exists before deleting
        leadRepository.findById(leadId)
            ?: throw LeadNotFoundException("Lead with ID $id not found")

        leadRepository.deleteById(leadId)
    }
}

