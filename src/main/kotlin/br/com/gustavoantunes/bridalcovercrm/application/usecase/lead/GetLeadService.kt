package br.com.gustavoantunes.bridalcovercrm.application.usecase.lead

import br.com.gustavoantunes.bridalcovercrm.application.dto.lead.GetLeadQuery
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.GetLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.out.repository.LeadRepository
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import org.springframework.stereotype.Service

@Service
class GetLeadService(
    private val leadRepository: LeadRepository
) : GetLeadUseCase {

    override fun execute(query: GetLeadQuery): Lead? {
        return leadRepository.findById(query.leadId)
    }
}


