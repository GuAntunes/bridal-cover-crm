package br.com.gustavoantunes.bridalcovercrm.application.service.lead

import br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead.GetLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.application.port.out.repository.LeadRepository
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import org.springframework.stereotype.Service

@Service
class GetLeadService(
    private val leadRepository: LeadRepository
) : GetLeadUseCase {

    override fun getLeadById(leadId: LeadId): Lead? {
        return leadRepository.findById(leadId)
    }
}
